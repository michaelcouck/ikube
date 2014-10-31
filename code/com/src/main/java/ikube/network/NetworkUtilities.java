package ikube.network;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * This class contains methods for scanning the network.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 23-10-2014
 */
public class NetworkUtilities {

    static class InetRange {
        public static int ipToInt(String ipAddress) {
            try {
                byte[] bytes = InetAddress.getByName(ipAddress).getAddress();
                int octet1 = (bytes[0] & 0xFF) << 24;
                int octet2 = (bytes[1] & 0xFF) << 16;
                int octet3 = (bytes[2] & 0xFF) << 8;
                int octet4 = bytes[3] & 0xFF;
                return octet1 | octet2 | octet3 | octet4;
            } catch (final Exception e) {
                LOGGER.error("Exception looking up ip address : " + ipAddress, e);
                return 0;
            }
        }

        public static String intToIp(int ipAddress) {
            int octet1 = (ipAddress & 0xFF000000) >>> 24;
            int octet2 = (ipAddress & 0xFF0000) >>> 16;
            int octet3 = (ipAddress & 0xFF00) >>> 8;
            int octet4 = ipAddress & 0xFF;
            return String.valueOf(octet1) + '.' + octet2 + '.' + octet3 + '.' + octet4;
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkUtilities.class);

    public static String[] scanNetwork(final String ipRange, final int reachableInterval) throws Exception {
        List<String> ipAddressesReachable = new ArrayList<>();
        int[] bounds = rangeFromCidr(ipRange);
        for (int i = bounds[0]; i <= bounds[1]; i++) {
            String address = InetRange.intToIp(i);
            InetAddress ip = InetAddress.getByName(address);
            if (ip.isReachable(reachableInterval)) {
                ipAddressesReachable.add(ip.getHostAddress());
            }
        }
        return ipAddressesReachable.toArray(new String[ipAddressesReachable.size()]);
    }

    public static int[] rangeFromCidr(String cidrIp) {
        int maskStub = 1 << 31;
        String[] atoms = cidrIp.split("/");
        int mask = Integer.parseInt(atoms[1]);
        LOGGER.info("Ip mask : " + mask);

        int[] result = new int[2];
        result[0] = InetRange.ipToInt(atoms[0]) & (maskStub >> (mask - 1)); // lower bound
        result[1] = InetRange.ipToInt(atoms[0]); // upper bound

        LOGGER.info("Lower ip address : " + InetRange.intToIp(result[0]));
        LOGGER.info("Upper ip address : " + InetRange.intToIp(result[1]));

        return result;
    }

}