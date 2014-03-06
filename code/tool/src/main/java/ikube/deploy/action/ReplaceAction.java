package ikube.deploy.action;

import au.com.bytecode.opencsv.CSVReader;
import ikube.deploy.model.Server;
import ikube.toolkit.FileUtilities;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.connection.channel.direct.Session;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * This action will replace all the proeprties in files on the target machine with properties defined
 * in the csv file with a specific environment, like production and development for example.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 06-03-2014
 */
public class ReplaceAction extends Action {

    private String environment;
    private Collection<String> fileNames;

    @Override
    @SuppressWarnings("EmptyFinallyBlock")
    public boolean execute(final Server server) throws Exception {
        final SSHClient sshExec = getSshExec(server.getIp(), server.getUsername(), server.getPassword());
        try {
            if (fileNames != null) {
                for (final String fileName : fileNames) {
                    try {
                        logger.info("Replacing from file : {} on machine : {}", new Object[]{fileName, server.getIp()});
                        File file = FileUtilities.findFileRecursively(new File("."), fileName);

                        Reader reader = new FileReader(file);
                        CSVReader csvReader = new CSVReader(reader, ',');
                        String[] headers = csvReader.readNext();
                        int index = 0;
                        for (int i = 0; i < headers.length; i++) {
                            if (headers[i].equals(environment)) {
                                index = i;
                                break;
                            }
                        }
                        String[] values = csvReader.readNext();
                        while (values != null && values.length > 0) {
                            if (values.length >= index) {
                                try (Session session = sshExec.startSession()) {
                                    Session.Command sessionCommand = session.exec(values[index]);
                                    sessionCommand.join(60, TimeUnit.SECONDS);
                                    Integer exitStatus = sessionCommand.getExitStatus();
                                    String errorMessage = sessionCommand.getExitErrorMessage();
                                    if (exitStatus != null && exitStatus > 0) {
                                        String message = IOUtils.readFully(sessionCommand.getInputStream()).toString();
                                        String error = IOUtils.readFully(sessionCommand.getErrorStream()).toString();
                                        Object[] parameters = {message, errorMessage, error, exitStatus};
                                        logger.info("Message : {}, error message : {}, error : {}, exit status : {}", parameters);
                                    } else if (StringUtils.isNotEmpty(errorMessage)) {
                                        logger.info("Error message : " + errorMessage);
                                    }
                                }
                            }
                        }
                    } catch (final Exception e) {
                        handleException("Exception replacing properties from file : " + fileName + ", server : " + server.getIp(), e);
                    }
                }
            }
        } finally {
            disconnect(sshExec);
        }
        return Boolean.TRUE;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public void setFileNames(final Collection<String> filesNames) {
        this.fileNames = filesNames;
    }

}
