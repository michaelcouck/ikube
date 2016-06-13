package ikube.post;

import ikube.jms.Publisher;
import ikube.jms.connect.WebSphereConnector;

public class QueuePoster {

    public static void main(final String[] args) throws Exception {
        new Publisher().publish(
                "admin", // User name
                "password", // Password
                "iiop://192.168.1.12:2809", // Url : 2809
                "cell/nodes/app1/servers/OPFClusterApp1/jms/QCF", // Connection factory name
                "cell/nodes/app1/servers/OPFClusterApp1/jms/UploadQ", // Destination name
                "uploadAction:txnTimeout:fileName", // Header names
                "synch:60:/home/wasadmin/upload/customers.txt", // Header values
                "Hello you bitch", // Payload
                WebSphereConnector.class.getName(), // Connector type
                1, // Iterations
                null // Folder name
        );
    }

}
