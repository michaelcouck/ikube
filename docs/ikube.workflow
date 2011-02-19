<?xml version="1.0" encoding="UTF-8"?>
<core:Model xmi:version="2.0" xmlns:xmi="http://www.omg.org/XMI" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:core="org.eclipse.jwt/core" xmlns:data="org.eclipse.jwt/data" xmlns:events="org.eclipse.jwt/events" xmlns:processes="org.eclipse.jwt/processes" name="Workflow" author="Michael Couck" version="" fileversion="1.0.0">
  <subpackages name="Applications">
    <ownedComment text="The standard package for applications"/>
  </subpackages>
  <subpackages name="Roles">
    <ownedComment text="The standard package for roles"/>
  </subpackages>
  <subpackages name="Data">
    <ownedComment text="The standard package for data"/>
    <subpackages name="Datatypes">
      <ownedComment text="The standard package for datatypes"/>
      <elements xsi:type="data:DataType" name="URL"/>
      <elements xsi:type="data:DataType" name="dioParameter"/>
      <elements xsi:type="data:DataType" name="qualifier"/>
      <elements xsi:type="data:DataType" name="searchquery"/>
      <elements xsi:type="data:DataType" name="filename"/>
    </subpackages>
  </subpackages>
  <elements xsi:type="processes:Activity" name="Synchroinsation">
    <ownedComment text="This is a basic activity"/>
    <nodes xsi:type="processes:InitialNode" name="Start Trigger" out="//@elements.0/@edges.0"/>
    <nodes xsi:type="processes:FinalNode" name="Wait for another timed trigger event." in="//@elements.0/@edges.16"/>
    <nodes xsi:type="events:Event" name="Timed Event" in="//@elements.0/@edges.0" out="//@elements.0/@edges.6"/>
    <nodes xsi:type="processes:DecisionNode" name="Are any servers working on this index (Faq)?" in="//@elements.0/@edges.10" out="//@elements.0/@edges.11"/>
    <nodes xsi:type="processes:DecisionNode" name="Is there a new index, that is not locked?" in="//@elements.0/@edges.9" out="//@elements.0/@edges.8 //@elements.0/@edges.10"/>
    <nodes xsi:type="processes:DecisionNode" name="Is the old index closed?" in="//@elements.0/@edges.6" out="//@elements.0/@edges.7 //@elements.0/@edges.9 //@elements.0/@edges.12"/>
    <nodes xsi:type="processes:DecisionNode" name="Is the current index past it's expiry date? Are there other servers working?" in="//@elements.0/@edges.12" out="//@elements.0/@edges.13 //@elements.0/@edges.14"/>
    <nodes xsi:type="processes:DecisionNode" name="Is the new index ready and can se delete the old ones?" in="//@elements.0/@edges.13" out="//@elements.0/@edges.15"/>
    <nodes xsi:type="processes:Action" name="Reset" in="//@elements.0/@edges.11" out="//@elements.0/@edges.1"/>
    <nodes xsi:type="processes:Action" name="Close" in="//@elements.0/@edges.8" out="//@elements.0/@edges.2"/>
    <nodes xsi:type="processes:Action" name="Open" in="//@elements.0/@edges.7" out="//@elements.0/@edges.3"/>
    <nodes xsi:type="processes:Action" name="Index" in="//@elements.0/@edges.14" out="//@elements.0/@edges.4"/>
    <nodes xsi:type="processes:Action" name="Delete" in="//@elements.0/@edges.15" out="//@elements.0/@edges.5"/>
    <nodes xsi:type="processes:MergeNode" name="Exit the index engine." in="//@elements.0/@edges.1 //@elements.0/@edges.2 //@elements.0/@edges.3 //@elements.0/@edges.4 //@elements.0/@edges.5" out="//@elements.0/@edges.16"/>
    <edges source="//@elements.0/@nodes.0" target="//@elements.0/@nodes.2"/>
    <edges source="//@elements.0/@nodes.8" target="//@elements.0/@nodes.13"/>
    <edges source="//@elements.0/@nodes.9" target="//@elements.0/@nodes.13"/>
    <edges source="//@elements.0/@nodes.10" target="//@elements.0/@nodes.13"/>
    <edges source="//@elements.0/@nodes.11" target="//@elements.0/@nodes.13"/>
    <edges source="//@elements.0/@nodes.12" target="//@elements.0/@nodes.13"/>
    <edges source="//@elements.0/@nodes.2" target="//@elements.0/@nodes.5"/>
    <edges source="//@elements.0/@nodes.5" target="//@elements.0/@nodes.10"/>
    <edges source="//@elements.0/@nodes.4" target="//@elements.0/@nodes.9"/>
    <edges source="//@elements.0/@nodes.5" target="//@elements.0/@nodes.4"/>
    <edges source="//@elements.0/@nodes.4" target="//@elements.0/@nodes.3"/>
    <edges source="//@elements.0/@nodes.3" target="//@elements.0/@nodes.8"/>
    <edges source="//@elements.0/@nodes.5" target="//@elements.0/@nodes.6"/>
    <edges source="//@elements.0/@nodes.6" target="//@elements.0/@nodes.7"/>
    <edges source="//@elements.0/@nodes.6" target="//@elements.0/@nodes.11"/>
    <edges source="//@elements.0/@nodes.7" target="//@elements.0/@nodes.12"/>
    <edges source="//@elements.0/@nodes.13" target="//@elements.0/@nodes.1"/>
  </elements>
  <elements xsi:type="data:Data" name="Servers Cluster State, Hazelcast" value="Clustered"/>
</core:Model>
