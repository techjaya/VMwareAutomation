switchList		1	10.20.49.122  <Switch IP's Followed by the Number>
vCenteripList		1	10.20.49.137  <vCenter IP Followed by the Number. Valid #: 1>
vCenternameList		1	vCenter1	<vCenter Name which is configured in the Fabric. Valid #: 1>
unameList		1	administrator	<UserName for the vCenter Server>
passwordList		1	pass	<Password for the vCenter Server>
datacenterList		1	AutoDC	<Data Center Name in the vCenter Server. Valid #: 1>
hostList		4	10.20.49.144 10.20.49.145	<ESX host list followed by the number. All ESX Hosts should belong to the same vCenter Server version>
10.20.49.144		4	vmnic8	vmnic9	vmnic10	vmnic11 <List of vmnics on each server. Each ESXi host vmnic list should be listed from this line onwards>
10.20.49.145		4	vmnic8	vmnic9	vmnic10	vmnic11
10.20.49.144		4	27/0/2	27/0/1	28/0/2	28/0/1	<List of switch interfaces corresponding to server vmnics. Each ESX host interfaces should be listed>
10.20.49.145		4	22/0/2	22/0/1	23/0/2	23/0/1
datastoreList		4	datastore49144 datastore49145 	<DataStore list corresponding to the ESX hosts. Valid #: Has to match the # in the Host List>
vswitchList		2	vSwitch101 vSwitch102	<Standard vSwitch List followed by the number>
vSwitch101		1	vmnic8 		<List of uplinks for each vSwitch. Each vSwitch uplinks should be listed from this point onwards>
vSwitch102		1	vmnic9
vSwitch101		2	stdpg101 stdpg102 	<List of standard portgroups per vSwitch. Each vSwitch portgroups should be listed>
vSwitch102		2	stdpg103 stdpg104
vSwitch101		2	101	102 	<Corresponding vlan for each standard port group. Number has to match the number of standard pgs>
vSwitch102		2	103	104
dvswitchList		2	dvSwitch101 dvSwitch102 	<DVSwitch List>
dvSwitch101		1	vmnic10 	<Uplink list for each dvSwitch>
dvSwitch102		1	vmnic11
dvSwitch101		2	dvpg101	dvpg102 <PortGroup list for each dvSwitch>
dvSwitch102		2	dvpg103	dvpg104
dvSwitch101		2	101	102 	<VLAN list for each dvswitch>
dvSwitch102		2	103	104
dvSwitch101		1	cdp	<CDP/LLDP option for each dvSwitch>
dvSwitch102		1	lldp
vmList 			2		<Number of VMs to be created. No need to list the names>	
stdvnicList		4 	stdpg101 stdpg102 stdpg103 stdpg104	<Number of standard vnics per each vm. These vnics has to match the vSwitch port group list>
dvnicList		4	dvpg101 dvpg102 dvpg103 dvpg104		<Number of dist. vnics per each vm. These vnics has to match the DVswitch port group list>
=============================================
Update Functions: From this point onwards all the parameters correspond to
update functionalities of already perform operation. (i.e: Adding port groups/
Deleting vSwithces etc)
=============================================
addpgList		2	stdaddpg101 stdaddpg102		<Number of standard port groups to be added>
stdaddpg101		1	vSwitch101	<List of corresponding vSwitches>
stdaddpg102		1	vSwitch102
adddvpgList		2	dvaddpg101 dvaddpg102 		<Number of DVPGs to be added>
dvaddpg101		1	dvSwitch101		<List of corresponding dvswitches>
dvaddpg102		1	dvSwitch102
addpgvlanList		1	1000		<VLAN to be changed. We will go ahead and change the "addpgList" to this vlan. Valid #: 1>
adddvpgvlanList		3	0-4 10-12 30-32 <VLANs to be changed. We will change the "addvpgList" to these VLANs.>
addstdvmkList		3		<Number of Standard VM Kernels to be added>
deletehostList		1	10.20.49.144	<List of hosts to be deleted>
deletevssList		2	vSwitch101 vSwitch102 <List of vSwitches to be deleted>
deletedvsList		2	dvSwitch101 dvSwitch102 <List of DvSwitches to be deleted>
