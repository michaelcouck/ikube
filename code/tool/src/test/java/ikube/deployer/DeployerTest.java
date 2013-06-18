package ikube.deployer;

import static ikube.toolkit.ObjectToolkit.populateFields;
import ikube.AbstractTest;
import ikube.deploy.action.CommandAction;
import ikube.deploy.action.CopyAction;
import ikube.deploy.action.IAction;
import ikube.deploy.model.Command;
import ikube.deploy.model.Directory;
import ikube.deploy.model.File;
import ikube.deploy.model.Server;
import ikube.toolkit.ObjectToolkit;
import ikube.toolkit.SerializationUtilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

public class DeployerTest extends AbstractTest {

	@Test
	public void serialize() {
		Server server = populateFields(new Server(), Boolean.TRUE, Integer.MAX_VALUE);
		CommandAction stopAction = populateFields(new CommandAction(), Boolean.TRUE, Integer.MAX_VALUE);
		stopAction.setCommands(new ArrayList<Command>(Arrays.asList(populateFields(new Command(), Boolean.TRUE, Integer.MAX_VALUE))));

		CopyAction copyAction = populateFields(new CopyAction(), Boolean.TRUE, Integer.MAX_VALUE);
		Map<Directory, Directory> directories = new HashMap<Directory, Directory>();
		directories.put(populateFields(new Directory(), Boolean.TRUE, Integer.MAX_VALUE), populateFields(new Directory(), Boolean.TRUE, Integer.MAX_VALUE));
		copyAction.setDirectories(directories);

		Map<File, File> files = new HashMap<File, File>();
		files.put(populateFields(new File(), Boolean.TRUE, Integer.MAX_VALUE), populateFields(new File(), Boolean.TRUE, Integer.MAX_VALUE));
		copyAction.setFiles(files);

		CommandAction startAction = ObjectToolkit.populateFields(new CommandAction(), Boolean.TRUE, Integer.MAX_VALUE);
		startAction.setCommands(new ArrayList<Command>(Arrays.asList(populateFields(new Command(), Boolean.TRUE, Integer.MAX_VALUE))));

		List<IAction> actions = new ArrayList<IAction>(Arrays.asList(stopAction, copyAction, startAction));
		server.setActions(actions);

		String xml = SerializationUtilities.serialize(server);
		logger.info(xml);
	}

}
