rem Where: <path-to-file>  the path to the file to load
rem        <group-id>      the group that the file should be registered under
rem        <artifact-id>   the artifact name for the file
rem        <version>       the version of the file
rem        <packaging>     the packaging of the file e.g. jar

rem This file needs to be executed, one line at a time to deploy the jars to the local repository.

rem mvn install:install-file -Dfile=db2jcc-9.5.jar -DgroupId=com.ibm.db2.jcc -DartifactId=db2jcc -Dversion=9.5 -Dpackaging=jar -DgeneratePom=true
rem mvn install:install-file -Dfile=db2jcc_license_cu-9.5.jar -DgroupId=com.ibm.db2.jcc_license_cu -DartifactId=db2jcc_license_cu -Dversion=9.5 -Dpackaging=jar -DgeneratePom=true
rem mvn install:install-file -Dfile=ojdbc6-11.jar -DgroupId=oracle.ojdbc6 -DartifactId=ojdbc6 -Dversion=11 -Dpackaging=jar -DgeneratePom=true

rem mvn install:install-file -Dfile=aspose-pdf-jdk16-2.7.0.jar -DgroupId=aspose -DartifactId=aspose-pdf-jdk16 -Dversion=2.7.0 -Dpackaging=jar -DgeneratePom=true
mvn install:install-file -Dfile=aspose-pdf-kit-3.5.0.jar -DgroupId=aspose -DartifactId=aspose-pdf-kit -Dversion=3.5.0 -Dpackaging=jar -DgeneratePom=true