package controllers.tools;

/**
 * Created by Sebastian on 2017-05-15.
 */

public class CoordinateConverter {
    //Semi-major axis of the ellipsoid.
    private double axis;

    //Flattening of the ellipsoid.
    private double flattening;

    //Central meridian for the projection.
    private double central_meridian;

    //Scale on central meridian.
    private double scale;

    //Offset for origo
    private double false_northing;
    //Offset for origo.
    private double false_easting;


    private void bessel_params() {
        axis = 6377397.155; // Bessel 1841.
        flattening = 1.0 / 299.1528128; // Bessel 1841.
        central_meridian = Double.MIN_VALUE;
        scale = 1.0;
        false_northing = 0.0;
        false_easting = 1500000.0;
        central_meridian = 15.0 + 48.0 / 60.0 + 29.8 / 3600.0;
    }


    public double[] grid_to_geodetic(double x, double y) {
        bessel_params();
        double[] lat_lon = new double[2];
        if (central_meridian == Double.MIN_VALUE) {
            return lat_lon;
        }
        // Prepare ellipsoid-based stuff.
        double e2 = flattening * (2.0 - flattening);
        double n = flattening / (2.0 - flattening);
        double a_roof = axis / (1.0 + n) * (1.0 + n * n / 4.0 + n * n * n * n / 64.0);
        double delta1 = n / 2.0 - 2.0 * n * n / 3.0 + 37.0 * n * n * n / 96.0 - n * n * n * n / 360.0;
        double delta2 = n * n / 48.0 + n * n * n / 15.0 - 437.0 * n * n * n * n / 1440.0;
        double delta3 = 17.0 * n * n * n / 480.0 - 37 * n * n * n * n / 840.0;
        double delta4 = 4397.0 * n * n * n * n / 161280.0;

        double Astar = e2 + e2 * e2 + e2 * e2 * e2 + e2 * e2 * e2 * e2;
        double Bstar = -(7.0 * e2 * e2 + 17.0 * e2 * e2 * e2 + 30.0 * e2 * e2 * e2 * e2) / 6.0;
        double Cstar = (224.0 * e2 * e2 * e2 + 889.0 * e2 * e2 * e2 * e2) / 120.0;
        double Dstar = -(4279.0 * e2 * e2 * e2 * e2) / 1260.0;

        // Convert.
        double deg_to_rad = Math.PI / 180;
        double lambda_zero = central_meridian * deg_to_rad;
        double xi = (x - false_northing) / (scale * a_roof);
        double eta = (y - false_easting) / (scale * a_roof);
        double xi_prim = xi -
                delta1 * Math.sin(2.0 * xi) * math_cosh(2.0 * eta) -
                delta2 * Math.sin(4.0 * xi) * math_cosh(4.0 * eta) -
                delta3 * Math.sin(6.0 * xi) * math_cosh(6.0 * eta) -
                delta4 * Math.sin(8.0 * xi) * math_cosh(8.0 * eta);
        double eta_prim = eta -
                delta1 * Math.cos(2.0 * xi) * math_sinh(2.0 * eta) -
                delta2 * Math.cos(4.0 * xi) * math_sinh(4.0 * eta) -
                delta3 * Math.cos(6.0 * xi) * math_sinh(6.0 * eta) -
                delta4 * Math.cos(8.0 * xi) * math_sinh(8.0 * eta);
        double phi_star = Math.asin(Math.sin(xi_prim) / math_cosh(eta_prim));
        double delta_lambda = Math.atan(math_sinh(eta_prim) / Math.cos(xi_prim));
        double lon_radian = lambda_zero + delta_lambda;
        double lat_radian = phi_star + Math.sin(phi_star) * Math.cos(phi_star) *
                (Astar +
                        Bstar * Math.pow(Math.sin(phi_star), 2) +
                        Cstar * Math.pow(Math.sin(phi_star), 4) +
                        Dstar * Math.pow(Math.sin(phi_star), 6));
        lat_lon[0] = lat_radian * 180.0 / Math.PI;
        lat_lon[1] = lon_radian * 180.0 / Math.PI;
        return lat_lon;
    }

    private double math_sinh(double value) {
        return 0.5 * (Math.exp(value) - Math.exp(-value));
    }

    private double math_cosh(double value) {
        return 0.5 * (Math.exp(value) + Math.exp(-value));
    }
}