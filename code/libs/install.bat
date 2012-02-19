rem Where: <path-to-file>  the path to the file to load
rem        <group-id>      the group that the file should be registered under
rem        <artifact-id>   the artifact name for the file
rem        <version>       the version of the file
rem        <packaging>     the packaging of the file e.g. jar
rem

rem This file needs to be executed, one line at a time to deploy the jars to the local repository.

mvn install:install-file -Dfile=drivers/db2jcc-9.5.jar -DgroupId=com.ibm.db2.jcc -DartifactId=db2jcc -Dversion=9.5 -Dpackaging=jar -DgeneratePom=true
mvn install:install-file -Dfile=drivers/db2jcc_license_cu-9.5.jar -DgroupId=com.ibm.db2.jcc_license_cu -DartifactId=db2jcc_license_cu -Dversion=9.5 -Dpackaging=jar -DgeneratePom=true
mvn install:install-file -Dfile=drivers/db2jcc4-9.7.2.jar -DgroupId=com.ibm.db2.jcc -DartifactId=db2jcc4 -Dversion=9.7.2 -Dpackaging=jar -DgeneratePom=true
mvn install:install-file -Dfile=drivers/db2jcc_license_cu-9.7.2.jar -DgroupId=com.ibm.db2.jcc_license_cu -DartifactId=db2jcc_license_cu -Dversion=9.7.2 -Dpackaging=jar -DgeneratePom=true

mvn install:install-file -Dfile=drivers/ojdbc6-11.jar -DgroupId=oracle.ojdbc6 -DartifactId=ojdbc6 -Dversion=11 -Dpackaging=jar -DgeneratePom=true

mvn install:install-file -Dfile=tools/jmxtools-1.2.1.jar -DgroupId=com.sun.jdmk -DartifactId=jmxtools -Dversion=1.2.1 -Dpackaging=jar -DgeneratePom=true
mvn install:install-file -Dfile=tools/jmxri-1.2.1.jar -DgroupId=com.sun.jmx -DartifactId=jmxri -Dversion=1.2.1 -Dpackaging=jar -DgeneratePom=true

mvn install:install-file -Dfile=tools/jep-2.3.1.jar -DgroupId=org.nfunk -DartifactId=jep -Dversion=2.3.1 -Dpackaging=jar -DgeneratePom=true
mvn install:install-file -Dfile=tools/jep-2.4.1.jar -DgroupId=org.nfunk -DartifactId=jep -Dversion=2.4.1 -Dpackaging=jar -DgeneratePom=true
mvn install:install-file -Dfile=tools/ext-1.1.1.jar -DgroupId=org.nfunk -DartifactId=ext -Dversion=1.1.1 -Dpackaging=jar -DgeneratePom=true

mvn install:install-file -Dfile=tools/canyon-0.9.8.jar -DgroupId=canyon -DartifactId=canyon -Dversion=0.9.8 -Dpackaging=jar -DgeneratePom=true

mvn install:install-file -Dfile=aspose/aspose-cells-7.0.0.jar -DgroupId=aspose -DartifactId=aspose.cells -Dversion=7.0.0 -Dpackaging=jar -DgeneratePom=true
mvn install:install-file -Dfile=aspose/aspose-pdf-2.9.0-jdk16.jar -DgroupId=aspose -DartifactId=aspose.pdf -Dversion=2.9.0 -Dpackaging=jar -DgeneratePom=true
mvn install:install-file -Dfile=aspose/aspose.slides-2.6.0.jar -DgroupId=aspose -DartifactId=aspose.slides -Dversion=2.6.0 -Dpackaging=jar -DgeneratePom=true
mvn install:install-file -Dfile=aspose/aspose.words.jdk16-7.0.0.jar -DgroupId=aspose -DartifactId=aspose.words -Dversion=7.0.0 -Dpackaging=jar -DgeneratePom=true
