rem Where: <path-to-file>  the path to the file to load
rem        <group-id>      the group that the file should be registered under
rem        <artifact-id>   the artifact name for the file
rem        <version>       the version of the file
rem        <packaging>     the packaging of the file e.g. jar

rem This file needs to be executed, one line at a time to deploy the jars to the local repository.

rem mvn install:install-file -Dfile=jsapi-1.0.jar -DgroupId=javax.speech -DartifactId=jsapi -Dversion=1.0 -Dpackaging=jar -DgeneratePom=true
rem mvn install:install-file -Dfile=sphinx4-1.0.jar -DgroupId=edu.cmu.sphinx -DartifactId=sphinx4 -Dversion=1.0 -Dpackaging=jar -DgeneratePom=true
rem mvn install:install-file -Dfile=db2jcc-9.5.jar -DgroupId=com.ibm.db2.jcc -DartifactId=db2jcc -Dversion=9.5 -Dpackaging=jar -DgeneratePom=true
rem mvn install:install-file -Dfile=db2jcc_license_cu-9.5.jar -DgroupId=com.ibm.db2.jcc_license_cu -DartifactId=db2jcc_license_cu -Dversion=9.5 -Dpackaging=jar -DgeneratePom=true
rem mvn install:install-file -Dfile=ojdbc6-11.jar -DgroupId=oracle.ojdbc6 -DartifactId=ojdbc6 -Dversion=11 -Dpackaging=jar -DgeneratePom=true
rem mvn install:install-file -Dfile=UmlGraph-4.3.jar -DgroupId=gr.spinellis -DartifactId=UmlGraph -Dversion=4.3 -Dpackaging=jar -DgeneratePom=true
rem mvn install:install-file -Dfile=pdfbox-1.3.1.jar -DgroupId=org.apache -DartifactId=pdfbox -Dversion=1.3.1 -Dpackaging=jar -DgeneratePom=true
rem mvn install:install-file -Dfile=fontbox-1.3.1.jar -DgroupId=org.apache -DartifactId=fontbox -Dversion=1.3.1 -Dpackaging=jar -DgeneratePom=true
rem mvn install:install-file -Dfile=jempbox-1.3.1.jar -DgroupId=org.apache -DartifactId=jempbox -Dversion=1.3.1 -Dpackaging=jar -DgeneratePom=true