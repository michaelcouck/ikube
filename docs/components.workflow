<?xml version="1.0" encoding="UTF-8"?>
<core:Model xmi:version="2.0" xmlns:xmi="http://www.omg.org/XMI" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:application="org.eclipse.jwt/application" xmlns:core="org.eclipse.jwt/core" xmlns:data="org.eclipse.jwt/data" xmlns:processes="org.eclipse.jwt/processes" name="Components" author="Michael Couck" version="1.0" fileversion="1.0.0">
  <elements xsi:type="processes:Activity" name="Components">
    <ownedComment text="This is a basic activity"/>
  </elements>
  <elements xsi:type="application:Application" name="Server One"/>
  <elements xsi:type="application:Application" name="Server Two"/>
  <elements xsi:type="application:Application" name="Server Three"/>
  <elements xsi:type="application:WebServiceApplication" name="Server One Web Service"/>
  <elements xsi:type="application:WebServiceApplication" name="Server Two Web Service"/>
  <elements xsi:type="application:WebServiceApplication" name="Server Three Web Service"/>
  <elements xsi:type="data:Data" name="Server Cluster State, Hazelcast"/>
</core:Model>
