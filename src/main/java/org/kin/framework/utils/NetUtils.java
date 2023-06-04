package org.kin.framework.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.*;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author huangjianqin
 * @date 2018/1/28
 */
public class NetUtils {
    private static final Logger log = LoggerFactory.getLogger(NetUtils.class);

    /** 全开放ip */
    public static final String ANY = "0.0.0.0";
    /** localhost ip */
    public static final String LOCALHOST_IP = "127.0.0.1";
    /** localhost name */
    public static final String LOCALHOST_NAME = "localhost";
    /** ip地址正则匹配 */
    private static final Pattern IP_PATTERN = Pattern.compile("\\d{1,3}(\\.\\d{1,3}){3,5}:\\d{1,5}$");

    /** 本地address, 非localhost */
    public static final InetAddress LOCAL_ADDRESS = getLocalAddress0();
    /**
     * The {@link Inet4Address} that represents the IPv4 loopback address '127.0.0.1'
     */
    public static final Inet4Address LOCALHOST4 = createLocalhost4();

    /**
     * The {@link Inet6Address} that represents the IPv6 loopback address '::1'
     */
    public static final Inet6Address LOCALHOST6 = createLocalhost6();

    /**
     * The loopback {@link NetworkInterface} of the current machine
     */
    public static final NetworkInterface LOOPBACK_IF;
    /**
     * The {@link InetAddress} that represents the loopback address. If IPv6 stack is available, it will refer to
     * {@link #LOCALHOST6}.  Otherwise, {@link #LOCALHOST4}.
     */
    public static final InetAddress LOCALHOST;

    static {
        NetworkIfaceAndInetAddress loopback = determineLoopback(LOCALHOST4, LOCALHOST6);
        LOOPBACK_IF = loopback.iface();
        LOCALHOST = loopback.address();
    }

    // -------------------------------------------------------------------- valid ------------------------------------------------------

    /**
     * valid Inet4Address
     */
    private static boolean isValidAddress(InetAddress address) {
        if (address == null || address.isLoopbackAddress()) {
            return false;
        }
        String name = address.getHostAddress();
        return (name != null
                && !ANY.equals(name)
                && !LOCALHOST_IP.equals(name)
                && !LOCALHOST_NAME.equals(name)
                && checkHostPort(name));
    }

    /**
     * valid Inet6Address, if an ipv6 address is reachable.
     */
    private static boolean isValidV6Address(Inet6Address address) {
        boolean preferIpv6 = Boolean.getBoolean("java.net.preferIPv6Addresses");
        if (!preferIpv6) {
            return false;
        }
        try {
            return address.isReachable(100);
        } catch (IOException e) {
            // ignore
        }
        return false;
    }

    /**
     * normalize the ipv6 Address, convert scope name to scope id.
     * <p>
     * e.g.
     * convert
     * fe80:0:0:0:894:aeec:f37d:23e1%en0
     * to
     * fe80:0:0:0:894:aeec:f37d:23e1%5
     * <p>
     * The %5 after ipv6 address is called scope id.
     * see java doc of {@link Inet6Address} for more details.
     *
     * @param address the input address
     * @return the normalized address, with scope id converted to int
     */
    private static InetAddress normalizeV6Address(Inet6Address address) {
        String addr = address.getHostAddress();
        int i = addr.lastIndexOf('%');
        if (i > 0) {
            try {
                return InetAddress.getByName(addr.substring(0, i) + '%' + address.getScopeId());
            } catch (UnknownHostException e) {
                // ignore
            }
        }
        return address;
    }

    // ---------------------- find ip ----------------------

    private static InetAddress getLocalAddress0() {
        InetAddress localAddress = null;
        try {
            localAddress = InetAddress.getLocalHost();
            if (localAddress instanceof Inet6Address) {
                Inet6Address address = (Inet6Address) localAddress;
                if (isValidV6Address(address)) {
                    return normalizeV6Address(address);
                }
            } else if (isValidAddress(localAddress)) {
                return localAddress;
            }
        } catch (Throwable e) {
            ExceptionUtils.throwExt(e);
        }
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            if (null == interfaces) {
                return localAddress;
            }
            while (interfaces.hasMoreElements()) {
                try {
                    NetworkInterface network = interfaces.nextElement();
                    Enumeration<InetAddress> addresses = network.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        try {
                            InetAddress address = addresses.nextElement();
                            if (address instanceof Inet6Address) {
                                Inet6Address v6Address = (Inet6Address) address;
                                if (isValidV6Address(v6Address)) {
                                    return normalizeV6Address(v6Address);
                                }
                            } else if (isValidAddress(address)) {
                                return address;
                            }
                        } catch (Throwable e) {
                            ExceptionUtils.throwExt(e);
                        }
                    }
                } catch (Throwable e) {
                    ExceptionUtils.throwExt(e);
                }
            }
        } catch (Throwable e) {
            ExceptionUtils.throwExt(e);
        }
        return localAddress;
    }

    /**
     * 创建ipv4 localhost地址
     *
     * @return ipv4 localhost地址
     */
    private static Inet4Address createLocalhost4() {
        byte[] localhost4Bytes = {127, 0, 0, 1};

        Inet4Address localhost4 = null;
        try {
            localhost4 = (Inet4Address) InetAddress.getByAddress("localhost", localhost4Bytes);
        } catch (Exception e) {
            // We should not get here as long as the length of the address is correct.
            ExceptionUtils.throwExt(e);
        }

        return localhost4;
    }

    /**
     * 创建ipv6 localhost地址
     *
     * @return ipv6 localhost地址
     */
    private static Inet6Address createLocalhost6() {
        byte[] localhost6Bytes = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1};

        Inet6Address localhost6 = null;
        try {
            localhost6 = (Inet6Address) InetAddress.getByAddress("localhost", localhost6Bytes);
        } catch (Exception e) {
            // We should not get here as long as the length of the address is correct.
            ExceptionUtils.throwExt(e);
        }

        return localhost6;
    }

    /**
     * copy from io.netty.util.NetUtilInitializations
     */
    private static NetworkIfaceAndInetAddress determineLoopback(Inet4Address localhost4, Inet6Address localhost6) {
        // Retrieve the list of available network interfaces.
        List<NetworkInterface> ifaces = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            if (interfaces != null) {
                while (interfaces.hasMoreElements()) {
                    NetworkInterface iface = interfaces.nextElement();
                    // Use the interface with proper INET addresses only.
                    if (addressesFromNetworkInterface(iface).hasMoreElements()) {
                        ifaces.add(iface);
                    }
                }
            }
        } catch (SocketException e) {
            log.warn("failed to retrieve the list of available network interfaces", e);
        }

        // Find the first loopback interface available from its INET address (127.0.0.1 or ::1)
        // Note that we do not use NetworkInterface.isLoopback() in the first place because it takes long time
        // on a certain environment. (e.g. Windows with -Djava.net.preferIPv4Stack=true)
        NetworkInterface loopbackIface = null;
        InetAddress loopbackAddr = null;
        loop:
        for (NetworkInterface iface : ifaces) {
            for (Enumeration<InetAddress> i = addressesFromNetworkInterface(iface); i.hasMoreElements(); ) {
                InetAddress addr = i.nextElement();
                if (addr.isLoopbackAddress()) {
                    // Found
                    loopbackIface = iface;
                    loopbackAddr = addr;
                    break loop;
                }
            }
        }

        // If failed to find the loopback interface from its INET address, fall back to isLoopback().
        if (loopbackIface == null) {
            try {
                for (NetworkInterface iface : ifaces) {
                    if (iface.isLoopback()) {
                        Enumeration<InetAddress> i = addressesFromNetworkInterface(iface);
                        if (i.hasMoreElements()) {
                            // Found the one with INET address.
                            loopbackIface = iface;
                            loopbackAddr = i.nextElement();
                            break;
                        }
                    }
                }

                if (loopbackIface == null) {
                    log.warn("failed to find the loopback interface");
                }
            } catch (SocketException e) {
                log.warn("failed to find the loopback interface", e);
            }
        }

        if (loopbackIface != null) {
            // Found the loopback interface with an INET address.
            log.debug(
                    "loopback interface: {} ({}, {})",
                    loopbackIface.getName(), loopbackIface.getDisplayName(), loopbackAddr.getHostAddress());
        } else {
            // Could not find the loopback interface, but we can't leave LOCALHOST as null.
            // Use LOCALHOST6 or LOCALHOST4, preferably the IPv6 one.
            if (loopbackAddr == null) {
                try {
                    if (NetworkInterface.getByInetAddress(localhost6) != null) {
                        log.debug("using hard-coded IPv6 localhost address: {}", localhost6);
                        loopbackAddr = localhost6;
                    }
                } catch (Exception e) {
                    // Ignore
                } finally {
                    if (loopbackAddr == null) {
                        log.debug("using hard-coded IPv4 localhost address: {}", localhost4);
                        loopbackAddr = localhost4;
                    }
                }
            }
        }

        return new NetworkIfaceAndInetAddress(loopbackIface, loopbackAddr);
    }

    private static Enumeration<InetAddress> addressesFromNetworkInterface(final NetworkInterface intf) {
        Enumeration<InetAddress> addresses =
                AccessController.doPrivileged((PrivilegedAction<Enumeration<InetAddress>>) intf::getInetAddresses);
        // Android seems to sometimes return null even if this is not a valid return value by the api docs.
        // Just return an empty Enumeration in this case.
        // See https://github.com/netty/netty/issues/10045
        if (addresses == null) {
            return CollectionUtils.emptyEnumeration();
        }
        return addresses;
    }

    // ---------------------- tool ----------------------

    /**
     * 检查是否合法ip
     *
     * @param address ip地址
     * @return true表示合法的ip地址
     */
    public static boolean checkHostPort(String address) {
        return address.matches(IP_PATTERN.pattern());
    }

    /**
     * Find first valid IP from local network card
     *
     * @return first valid local IP
     */
    public static InetAddress getLocalAddress() {
        return LOCAL_ADDRESS;
    }

    /**
     * 获取local host address
     *
     * @return local host address
     */
    public static InetAddress getLocalhost() {
        return LOCALHOST;
    }

    /**
     * 返回local address ip address
     *
     * @return local address ip address
     */
    public static String getLocalAddressIp() {
        return getLocalAddress().getHostAddress();
    }

    /**
     * 返回localhost address ip address
     *
     * @return localhost address ip address
     */
    public static String getLocalhostIp() {
        return getLocalhost().getHostAddress();
    }

    /**
     * @param port 端口号
     * @return String ip:port
     */
    public static String getIpPort(int port) {
        String ip = getLocalhostIp();
        return getIpPort(ip, port);
    }

    public static String getIpPort(String ip, int port) {
        if (ip == null) {
            return null;
        }
        return ip.concat(":").concat(String.valueOf(port));
    }

    public static String getIpPort2(int port) {
        InetSocketAddress address = new InetSocketAddress(port);
        return address.toString();
    }

    public static String getIpPort2(String ip, int port) {
        InetSocketAddress address = new InetSocketAddress(ip, port);
        return address.toString();
    }

    public static Object[] parseIpPort(String address) {
        String[] array = address.split(":");

        String host = array[0];
        int port = Integer.parseInt(array[1]);

        return new Object[]{host, port};
    }

    /**
     * 检查端口是否在指定范围内
     *
     * @param port 端口号
     * @return 是否合法
     */
    public static boolean isPortInRange(int port) {
        return 0 <= port && port <= 0xFFFF;
    }

    /**
     * 检查端口是否被占用
     * @return true表示没有被占用
     */
    public static boolean isValidPort(int port) {
        return isValidPort(getLocalhostIp(), port);
    }

    /**
     * 检查端口是否被占用
     */
    public static boolean isValidPort(String host, int port) {
        if (StringUtils.isBlank(host) || !isPortInRange(port)) {
            return false;
        }

        try (Socket socket = new Socket()) {
            socket.bind(new InetSocketAddress(host, port));
            return true;
        } catch (IOException ignored) {
            //do nothing
        }

        return false;
    }

    /**
     * 读取远程服务器文件, 需保留两服务器连通
     */
    public static boolean copyRemoteFile(String url, String sinkFileName) {
        URL u;
        try {
            u = new URL(url);
            URLConnection uc = u.openConnection();
            //setting
            uc.setUseCaches(false);
            uc.setReadTimeout(5 * 1000);
            uc.setConnectTimeout(3 * 1000);

            String contentType = uc.getContentType();
            int contentLength = uc.getContentLength();
            if (contentLength != -1) {
                InputStream raw = uc.getInputStream();
                InputStream in = new BufferedInputStream(raw);
                byte[] data = new byte[contentLength];
                int bytesRead;
                int offset = 0;
                while (offset < contentLength) {
                    bytesRead = in.read(data, offset, data.length - offset);
                    if (bytesRead == -1) {
                        break;
                    }
                    offset += bytesRead;
                }
                in.close();

                if (offset != contentLength) {
                    throw new IOException("Only read " + offset + " bytes; Expected " + contentLength + " bytes");
                }

                File sinkFile = new File(sinkFileName);
                if (!sinkFile.exists()) {
                    sinkFile.getParentFile().mkdirs();
                }

                FileOutputStream out = new FileOutputStream(sinkFileName);
                out.write(data);
                out.flush();
                out.close();
            }

            return true;
        } catch (IOException e) {
            ExceptionUtils.throwExt(e);
        }

        return false;
    }

    /**
     * hash ip
     */
    public static long ipHashCode(String ip) {
        String[] splits = ip.split("/");
        if (splits.length > 1) {
            ip = splits[splits.length - 1];
        }
        splits = ip.split("\\.");
        long hashcode = 0L;
        int offset = 24;
        for (String item : splits) {
            hashcode += Long.parseLong(item) << offset;
            offset -= 8;
        }
        return hashcode;
    }

    /**
     * hash ip+port
     */
    public static long ipHashCode(String ip, int port) {
        return ipHashCode(ip) + port;
    }

    //---------------------------------------------------------------------------------------------------------
    static final class NetworkIfaceAndInetAddress {
        private final NetworkInterface iface;
        private final InetAddress address;

        NetworkIfaceAndInetAddress(NetworkInterface iface, InetAddress address) {
            this.iface = iface;
            this.address = address;
        }

        public NetworkInterface iface() {
            return iface;
        }

        public InetAddress address() {
            return address;
        }
    }
}
