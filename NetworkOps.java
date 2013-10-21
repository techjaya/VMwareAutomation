//@author Don Jayakody
package vCenterOps;

import java.net.URL;
import java.util.*;
import java.io.*;
import com.vmware.vim25.*;
import com.vmware.vim25.mo.*;
import com.vmware.vim25.mo.util.*;
import java.sql.Timestamp;

public class NetworkOps {

	public static String AddvSwitch(ServiceInstance si, ArrayList<String> vswitchList, ArrayList<String> hostList, Map<String, ArrayList<String>> pgMap, Map<String, ArrayList<Integer>> vlanMap, Map<String, ArrayList<String>> uplinkMap, ArrayList<String> datacenterList, boolean delayFlag) throws Exception {

	

		//Output the datacenterID+PG Names to a text file: /RunOutput/<datacenterName>PortGroup.txt
		String fileName = "./RunOutput/ADDPG_"+datacenterList.get(0)+"PortGroup.txt";
		BufferedWriter bw = writeFile(fileName);


		PrintMsg.HeaderPrint("NetworkOps Command Execution: AddvSwitch");
		String finalStatus = "SUCCESS";
		try {
			Folder rootFolder = si.getRootFolder();
			
			int eventCount = 0;
		
			for(int i=0; i<hostList.size(); i++) {
				
				String runTime = "runTime";
				if(i==0) {
					runTime = "FirstRun";
				} 

				HostSystem host = (HostSystem) new InventoryNavigator(rootFolder).searchManagedEntity("HostSystem", hostList.get(i));
				Datacenter currentDc = (Datacenter) host.getParent().getParent().getParent();
				String currentDcId= currentDc.getMOR().getVal();

				if(host==null) {	
					PrintMsg.ErrorPrint("AddvSwitch - Source Host " + hostList.get(i) + " not found");
					finalStatus = "FAIL";
					return finalStatus;
				}
				PrintMsg.ItemPrint("Adding vSwitches to Host " + hostList.get(i));
				HostNetworkSystem hns = host.getHostNetworkSystem();

				for (int j=0; j<vswitchList.size(); j++) {
					HostVirtualSwitchSpec spec = new HostVirtualSwitchSpec();
					spec.setNumPorts(8);
					PrintMsg.ItemPrint("Adding vSwitch: " + vswitchList.get(j));
					
					//Preparing uplinkList from passed uplinkMap
					ArrayList<String> uplinkList = uplinkMap.get(vswitchList.get(j));
					PrintMsg.ItemPrint("Enabling CDP for: " + vswitchList.get(j));
					HostVirtualSwitchBondBridge hvsbb = new HostVirtualSwitchBondBridge();
					LinkDiscoveryProtocolConfig ldpc = new LinkDiscoveryProtocolConfig();
					ldpc.setOperation("both");
					ldpc.setProtocol("cdp");
					hvsbb.setLinkDiscoveryProtocolConfig(ldpc);
					if (uplinkList != null && uplinkList.size() > 0) {
						String[] uplinkListArray = uplinkList.toArray(new String[uplinkList.size()]);
						hvsbb.setNicDevice(uplinkListArray);
						spec.setBridge(hvsbb);
					}

					if(delayFlag && eventCount != 0 && eventCount %3 == 0) {
						Thread.sleep(11000);
					}
					eventCount++;

					hns.addVirtualSwitch(vswitchList.get(j), spec);

					//Preparing pgList & vlanList from passed pgMap & vlanMap
					ArrayList<String> pgList = pgMap.get(vswitchList.get(j));
					ArrayList<Integer> vlanList = vlanMap.get(vswitchList.get(j));

					for(int k=0; k<pgList.size(); k++) {
						HostPortGroupSpec hpgs = new HostPortGroupSpec();
						hpgs.setName(pgList.get(k));
						hpgs.setVlanId(vlanList.get(k));
						hpgs.setVswitchName(vswitchList.get(j));
						hpgs.setPolicy(new HostNetworkPolicy());
						
						if(delayFlag && eventCount != 0 && eventCount %3 == 0) {
							Thread.sleep(11000);
						}
						eventCount++;

						PrintMsg.ItemPrint("Adding portGroup: " + pgList.get(k));
						hns.addPortGroup(hpgs);

						if (runTime.equals("FirstRun")) {
							bw.write(currentDcId + "_" + pgList.get(k) +  "\n");
						}
					}
				}
				
			}
			bw.close();
		} catch (Exception e) {
			PrintMsg.ErrorPrint("Error during creating vSwitches & PGs: " + e);
			finalStatus = "FAIL";
			return finalStatus;
		}
		return finalStatus;

	}


	public static String DeletevSwitch(ServiceInstance si, ArrayList<String> deletevssList, ArrayList<String> hostList , ArrayList<String> datacenterList, Map<String, ArrayList<String>> pgMap, boolean delayFlag) throws Exception {
	
		PrintMsg.HeaderPrint("NetworkOps Command Execution: DeletevSwitch");
		String finalStatus = "SUCCESS";

		try {


			Folder rootFolder = si.getRootFolder();

	
			//Output the datacenter value to a text file: /RunOutput/<datacenterName>PortGroup.txt
			String fileName = "./RunOutput/DELPG_"+datacenterList.get(0)+"PortGroup.txt";
			BufferedWriter bw = writeFile(fileName);
			int eventCount = 0;

			for(int i=0; i<hostList.size(); i++) {


				String runTime = "runTime";
				if(i==0) {
					runTime = "FirstRun";
				} 

				HostSystem host = (HostSystem) new InventoryNavigator(rootFolder).searchManagedEntity("HostSystem", hostList.get(i));
				if(host==null) {	
					PrintMsg.ErrorPrint("DeletevSwitch - Source Host " + hostList.get(i) + " Not Found");
					finalStatus = "FAIL";
					return finalStatus;
				}

				PrintMsg.ItemPrint("Deleting vSwitch From Host " + hostList.get(i));
				Datacenter currentDc = (Datacenter) host.getParent().getParent().getParent();
				String currentDcId= currentDc.getMOR().getVal();
				
				HostNetworkSystem hns = host.getHostNetworkSystem();

				for (int j=0; j<deletevssList.size(); j++) {

					if(delayFlag && eventCount != 0 && eventCount %3 == 0) {
						Thread.sleep(11000);
					}
					eventCount++;

					PrintMsg.ItemPrint("Deleting vSwitch: " + deletevssList.get(j));
					hns.removeVirtualSwitch(deletevssList.get(j));

					//Preparing pgList from passed pgMap
					ArrayList<String> pgList = pgMap.get(deletevssList.get(j));

					if(runTime.equals("FirstRun")) {
						for (int k=0; k<pgList.size(); k++) {
							bw.write(currentDcId + "_" + pgList.get(k) +  "\n");
						}
					}
				}



			}
			bw.close();

		} catch (Exception e) {
			PrintMsg.ErrorPrint("Error during deleting vSwitches & PGs: " + e);
			finalStatus = "FAIL";
			return finalStatus;
		}

		return finalStatus;
	}



	public static String AddDvSwitch (ServiceInstance si, ArrayList<String> dvswitchList, ArrayList<String> hostList, Map<String, ArrayList<String>> dvpgMap, Map<String, ArrayList<Integer>> dvpgvlanMap, Map<String, ArrayList<String>> dvpguplinkMap, Map<String, ArrayList<String>> dvsldpMap, ArrayList<String> datacenterList, boolean delayFlag) throws Exception {

		PrintMsg.HeaderPrint("NetworkOps Command Execution: addDvSwitch");
		String finalStatus = "SUCCESS";

		String fileName = "./RunOutput/ADDDvPG_"+datacenterList.get(0)+"PortGroup.txt";
		BufferedWriter bw = writeFile(fileName);
	

		try {

			Folder rootFolder = si.getRootFolder();
			Datacenter dc = (Datacenter) si.getSearchIndex().findByInventoryPath(datacenterList.get(0));
			String currentDcId= dc.getMOR().getVal();
			int eventCount = 0;

			for (int i=0; i<dvswitchList.size(); i++) {
				PrintMsg.ItemPrint("Adding dvSwitch: " + dvswitchList.get(i));
				Folder netFolder = dc.getNetworkFolder();
				DVSCreateSpec dcs = new DVSCreateSpec();
				DVSConfigSpec dcfg = new DVSConfigSpec();
				dcs.setConfigSpec(dcfg);
				dcfg.setName(dvswitchList.get(i));
				
				//Prepare the DVUplinks 
				ArrayList<String> uplinkList = dvpguplinkMap.get(dvswitchList.get(i));
				if(uplinkList.size() != 0) {
					DVSNameArrayUplinkPortPolicy dvsUplinks = new DVSNameArrayUplinkPortPolicy();
					String[] uplinkArray = new String[uplinkList.size()];
					for (int n=0; n<uplinkList.size(); n++) {
						uplinkArray[n] = "dvUplink"+ (n+1) ;
					}
					dvsUplinks.setUplinkPortName(uplinkArray);
					dcfg.setUplinkPortPolicy(dvsUplinks);
				}

				

				DistributedVirtualSwitchHostMemberConfigSpec[] dvsHosts = new DistributedVirtualSwitchHostMemberConfigSpec[hostList.size()];
				dcfg.setHost(dvsHosts);
				

				for (int j=0; j<hostList.size(); j++) {
					dvsHosts[j] = new DistributedVirtualSwitchHostMemberConfigSpec();
					dvsHosts[j].setOperation("add");

					HostSystem host = (HostSystem) new InventoryNavigator(rootFolder).searchManagedEntity("HostSystem", hostList.get(j));
					dvsHosts[j].setHost(host.getMOR());
					PrintMsg.ItemPrint("Adding Host: " + hostList.get(j) + " To dvSwitch: " + dvswitchList.get(i));

					DistributedVirtualSwitchHostMemberPnicBacking dvsPnic = new DistributedVirtualSwitchHostMemberPnicBacking();
					PhysicalNic[] pnics = ((HostSystem)host).getConfig().getNetwork().getPnic();
					DistributedVirtualSwitchHostMemberPnicSpec[] pnicSpecs = new DistributedVirtualSwitchHostMemberPnicSpec[uplinkList.size()];
					for (int k=0; k<uplinkList.size(); k++) {
						String curUplink = uplinkList.get(k);
						for (int m=0; m<pnics.length ; m++) {
							if (curUplink.equals(pnics[m].getDevice())) {
								PrintMsg.ItemPrint("Adding Physical Nic: " + pnics[m].getDevice());
								pnicSpecs[k] = new DistributedVirtualSwitchHostMemberPnicSpec();
								pnicSpecs[k].pnicDevice = pnics[m].getDevice();
							}
						}
					}

					dvsPnic.setPnicSpec(pnicSpecs);
					dvsHosts[j].setBacking(dvsPnic);

				}

				if(delayFlag && eventCount != 0 && eventCount %3 == 0) {
					Thread.sleep(11000);
				}
				eventCount++;

				Task task = netFolder.createDVS_Task(dcs);
				TaskInfo ti = waitFor(task);

				if(ti.getState() == TaskInfoState.error) {
					PrintMsg.ErrorPrint("Failed To Create A New dvSwitch: " + dvswitchList.get(i));
					System.exit(0);
				}


				ManagedObjectReference dvsMor = (ManagedObjectReference) ti.getResult();

				//Enable CDP or LLDP in the DvSwitch
				VmwareDistributedVirtualSwitch dvs = new VmwareDistributedVirtualSwitch(dc.getServerConnection(), dvsMor);
				String version = dvs.getConfig().configVersion; 
				VMwareDVSConfigSpec cfgSpec = new VMwareDVSConfigSpec();
				cfgSpec.configVersion = version;

				LinkDiscoveryProtocolConfig ldpCfg = new LinkDiscoveryProtocolConfig();
				cfgSpec.linkDiscoveryProtocolConfig = ldpCfg;
				ArrayList<String> dvsldpList = dvsldpMap.get(dvswitchList.get(i));
				ldpCfg.protocol = dvsldpList.get(0);
				ldpCfg.operation = "both";
				Task task_ldp = dvs.reconfigureDvs_Task(cfgSpec);
				TaskInfo ti1 = waitFor(task_ldp);
				if(ti1.getState() == TaskInfoState.error) {
					PrintMsg.ErrorPrint("Failed To Enable " +dvsldpList.get(0) + " In dvSwitch: " + dvswitchList.get(i));
					finalStatus = "FAIL";
				}else {
					PrintMsg.ItemPrint("Enabling " +dvsldpList.get(0) + " For: " + dvswitchList.get(i));
				}

				//Add the DVPGs to the newly created dvSwitch
				ArrayList<String> dvpgList = dvpgMap.get(dvswitchList.get(i));
				ArrayList<Integer> vlanList = dvpgvlanMap.get(dvswitchList.get(i));
				DVPortgroupConfigSpec[] dvpgs = new DVPortgroupConfigSpec[dvpgList.size()];
				for (int m=0; m<dvpgList.size(); m++) {
					dvpgs[m] = new DVPortgroupConfigSpec();
					dvpgs[m].setName(dvpgList.get(m));
					dvpgs[m].setNumPorts(128);
					dvpgs[m].setType("earlyBinding");
					VMwareDVSPortSetting vport = new VMwareDVSPortSetting();
					dvpgs[m].setDefaultPortConfig(vport);
					VmwareDistributedVirtualSwitchVlanIdSpec vlan = new VmwareDistributedVirtualSwitchVlanIdSpec();
					vport.setVlan(vlan);
					vlan.setInherited(false);
					vlan.setVlanId(vlanList.get(m));
					bw.write(currentDcId + "_" + dvpgList.get(m) +  "\n");
				}

				if(delayFlag && eventCount != 0 && eventCount %3 == 0) {
					Thread.sleep(11000);
				}
				eventCount++;
				
				Task task_pg = dvs.addDVPortgroup_Task(dvpgs);
				TaskInfo ti2 = waitFor(task_pg);

				if(ti2.getState() == TaskInfoState.error) {
					PrintMsg.ErrorPrint("Failed to create DVpgs to dvSwitch: " + dvswitchList.get(i));
				}


				ManagedObjectReference pgMor = (ManagedObjectReference) ti2.getResult();
				DistributedVirtualPortgroup pg = (DistributedVirtualPortgroup) MorUtil.createExactManagedEntity(dvs.getServerConnection(), pgMor);




			}
			bw.close();
		

		} catch (Exception e) {
			PrintMsg.ErrorPrint("Error during adding dvSwitches & PGs: " + e);
			finalStatus = "FAIL";
			return finalStatus;
		}

		return finalStatus;


	}


	private static TaskInfo waitFor(Task task) throws Exception {

		while (true) {

			TaskInfo ti = task.getTaskInfo();
			TaskInfoState state = ti.getState();
			if(state == TaskInfoState.success || state == TaskInfoState.error) {
				return ti;
			}
			
			Thread.sleep(1000);
			
		}
	}

	public static String DeleteDvSwitch(ServiceInstance si, ArrayList<String> deletedvsList, Map<String, ArrayList<String>> dvpgMap, ArrayList<String> datacenterList, boolean delayFlag) throws Exception { 
	
		PrintMsg.HeaderPrint("NetworkOps Command Execution: DeleteDvSwitch");
		String finalStatus = "SUCCESS";

		String fileName = "./RunOutput/DELDvPG_"+datacenterList.get(0)+"PortGroup.txt";
		BufferedWriter bw = writeFile(fileName);
		int eventCount = 0;

		try {

			Folder rootFolder = si.getRootFolder();
			Datacenter dc = (Datacenter) si.getSearchIndex().findByInventoryPath(datacenterList.get(0));
			String currentDcId= dc.getMOR().getVal();

			for (int i=0; i<deletedvsList.size(); i++) {
				PrintMsg.ItemPrint("Deleting dvSwitch: " + deletedvsList.get(i));

				DistributedVirtualSwitch dvSwitch = (DistributedVirtualSwitch) new InventoryNavigator(rootFolder).searchManagedEntity("DistributedVirtualSwitch", deletedvsList.get(i));
				if (dvSwitch == null) {
					PrintMsg.ErrorPrint("DeleteDvSwitch: dvSwitch " + deletedvsList.get(i) + " not found");
					finalStatus = "FAIL";
					return finalStatus;
				}

				if(delayFlag && eventCount != 0 && eventCount %3 == 0) {
					Thread.sleep(11000);
				}
				eventCount++;

				Task task = dvSwitch.destroy_Task();
				String status = task.waitForTask();

				if (!status.equals("success")) {
					PrintMsg.ErrorPrint("Error Deleting dvSwitch : " + deletedvsList.get(i));
					finalStatus = "FAIL";
					return finalStatus;
				}
			
				ArrayList<String> dvpgList = dvpgMap.get(deletedvsList.get(i));
				for (int k=0; k<dvpgList.size(); k++) {
					bw.write(currentDcId + "_" + dvpgList.get(k) +  "\n");
				}


			}
			bw.close();

		} catch (Exception e) {
			PrintMsg.ErrorPrint("Error Deleting dvSwitches: " + e);
			finalStatus = "FAIL";
			return finalStatus;
		}

		return finalStatus;
	}


	
	private static BufferedWriter writeFile(String fileName) throws Exception {
		try { 
			File file = new File(fileName);
			if (!file.exists()) {
				file.createNewFile();
			}
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			return bw;

		} catch (Exception e) {
			PrintMsg.ErrorPrint("Error Creating File: " + fileName + " - " + e);
			System.exit(0);
		}

		return null;	


	}

	public static String ChangeVnics(ServiceInstance si, ArrayList<String> hostList, Map<String, ArrayList<String>> vmMap, ArrayList<String> addpgList, Map<String, ArrayList<String>> addpgMap, ArrayList<String> changestdvnicList, Map<String, ArrayList<String>> changestdvnicMap, ArrayList<String> datacenterList) {

		PrintMsg.HeaderPrint("NetworkOps Command Execution: ChangeVnics");
		String finalStatus = "SUCCESS";

//		String fileName = "./RunOutput/UpdatePGAdd_"+datacenterList.get(0)+"PortGroup.txt";
		//BufferedWriter bw = writeFile(fileName);


		try {

			Folder rootFolder = si.getRootFolder();
			PrintMsg.MsgPrint("Changing Following vNICS : ");
			for (int e=0; e<addpgList.size(); e++) {
				PrintMsg.ItemPrint(addpgList.get(e));
			}

			for (int a=0; a<hostList.size(); a++) {
				ArrayList<String> vmList = vmMap.get(hostList.get(a)); 
				
				for (int i=0; i<vmList.size(); i++) {

					VirtualMachine vm = (VirtualMachine) new InventoryNavigator(rootFolder).searchManagedEntity("VirtualMachine", vmList.get(i));
					if(vm!=null) {

			
						VirtualMachineConfigSpec vmConfigSpec = new VirtualMachineConfigSpec();

						VirtualDeviceConfigSpec[] nicSpecArray = new VirtualDeviceConfigSpec[addpgList.size()];
						int numChangeVnics = changestdvnicList.size();
						//for (int j=0; j<addpgList.size(); j++) {
							VirtualDeviceConfigSpec nicSpec = new VirtualDeviceConfigSpec();
							VirtualMachineConfigInfo vmConfigInfo = vm.getConfig();

							nicSpec.setOperation(VirtualDeviceConfigSpecOperation.edit);

							VirtualDevice [] vds = vmConfigInfo.getHardware().getDevice();
							for (int k=0; k<vds.length; k++) {
								for(int j=0; j<addpgList.size(); j++) {
							
								       if ((vds[k] instanceof VirtualEthernetCard) && (vds[k].getDeviceInfo().getSummary().equalsIgnoreCase(addpgList.get(j)))) {
									       //System.out.println("VM is: " + vmList.get(i));
									       //System.out.println("Network adapter is: " + vds[k].getDeviceInfo().getLabel());
									       VirtualEthernetCard nic = (VirtualEthernetCard) vds[k];
									       VirtualDeviceBackingInfo properties = nic.getBacking();
									       VirtualEthernetCardNetworkBackingInfo nicBacking = (VirtualEthernetCardNetworkBackingInfo) properties;
									       if (numChangeVnics == 1) {
											nicBacking.setDeviceName(changestdvnicList.get(0));
											PrintMsg.ItemPrint("VM: " + vmList.get(i) + " - Changing Vnic To: " + changestdvnicList.get(0) + " - From: " + addpgList.get(j));
									       } else {
											nicBacking.setDeviceName(changestdvnicList.get(i));
											PrintMsg.ItemPrint("VM: " + vmList.get(i) + " - Changing Vnic To: " + changestdvnicList.get(i) + " - From: " + addpgList.get(j));
									       }
									       nic.setBacking(nicBacking);
									       nicSpec.setDevice(nic);
									       nicSpecArray[0] = nicSpec;
								       
							       		}	       
								}
							}
						//}
						vmConfigSpec.setDeviceChange(nicSpecArray);
						Task task = vm.reconfigVM_Task(vmConfigSpec);
                                                String status = task.waitForTask();
                                                if (!status.equals("success")) {
                                                        PrintMsg.ErrorPrint("Error Changing Standard vNics on VM : " + vmList.get(i));
                                                        finalStatus = "FAIL";
                                                        return finalStatus;
                                                }else {
                                                        //Before renaming the vm retrieve the MAC address information
//                                                        VirtualMachine reconfigVM = (VirtualMachine) new InventoryNavigator(rootFolder).searchManagedEntity("VirtualMachine", vmList.get(i));
 //                                                       writeVMMACstoFile(rootFolder, vmList.get(i), reconfigVM, dvswitchList, dvpgMap, bw);
                                                }
			
					
					}
				}
			}
			//bw.close();

		} catch (Exception e) {
			PrintMsg.ErrorPrint("Error Changing VNICs: " + e);
			finalStatus = "FAIL";
			return finalStatus;
		}
	

		return finalStatus;
	}


	public static String AddPGs(ServiceInstance si, ArrayList<String> hostList, ArrayList<String> addpgList, Map<String, ArrayList<String>> addpgMap,ArrayList<Integer>addpgvlanList, ArrayList<String> datacenterList, boolean delayFlag ) throws Exception { 

		PrintMsg.HeaderPrint("NetworkOps Command Execution: AddPGs");
		String finalStatus = "SUCCESS";

		String fileName = "./RunOutput/UpdatePGAdd_"+datacenterList.get(0)+"PortGroup.txt";
		BufferedWriter bw = writeFile(fileName);
	
		try {

			Folder rootFolder = si.getRootFolder();
			Datacenter dc = (Datacenter) si.getSearchIndex().findByInventoryPath(datacenterList.get(0));
			String currentDcId= dc.getMOR().getVal();
			int eventCount = 0;

			for (int i=0; i<hostList.size(); i++) {
				String runTime = "runTime";
				if(i==0) {
					runTime = "FirstRun";
				} 
	
				HostSystem host = (HostSystem) new InventoryNavigator(rootFolder).searchManagedEntity("HostSystem", hostList.get(i));
				HostNetworkSystem hns = host.getHostNetworkSystem();

				for (int j=0; j<addpgList.size(); j++) {

					ArrayList<String> addpgVSSList = addpgMap.get(addpgList.get(j));
					for (int k=0; k<addpgVSSList.size(); k++) {

						if(delayFlag && eventCount != 0 && eventCount %3 == 0) {
							Thread.sleep(11000);
						}
						eventCount++;

						HostPortGroupSpec pg = new HostPortGroupSpec();
						pg.setName(addpgList.get(j));
						pg.setVswitchName(addpgVSSList.get(k));
						if(addpgvlanList.size() == 1) {
							pg.setVlanId(addpgvlanList.get(0));
						} else {
							pg.setVlanId(addpgvlanList.get(j));
						}
						pg.setPolicy(new HostNetworkPolicy());
						PrintMsg.ItemPrint("Adding PG: " + addpgList.get(j) + " In to vSwitch: " + addpgVSSList.get(k) + " In Host: " + hostList.get(i)) ;
						hns.addPortGroup(pg);
						if (runTime.equals("FirstRun")) {
							bw.write(currentDcId + "_" + addpgList.get(j) +  "\n");
						}
					}
		
				}
			}
			bw.close();

		} catch (Exception e) {
			PrintMsg.ErrorPrint("Error Creating PGs: " + e);
			finalStatus = "FAIL";
			return finalStatus;
		}

		return finalStatus;

	}

	// This is an Over-Loaded AddPGs method to add a PG without a VLAN. This method will get invoked if a user doesn't pass the "addpgvlan" list.
	public static String AddPGs(ServiceInstance si, ArrayList<String> hostList, ArrayList<String> addpgList, Map<String, ArrayList<String>> addpgMap, ArrayList<String> datacenterList , boolean delayFlag) throws Exception { 

		PrintMsg.HeaderPrint("NetworkOps Command Execution: AddPGs");
		String finalStatus = "SUCCESS";

		String fileName = "./RunOutput/UpdatePGAdd_"+datacenterList.get(0)+"PortGroup.txt";
		BufferedWriter bw = writeFile(fileName);
	
		try {

			Folder rootFolder = si.getRootFolder();
			Datacenter dc = (Datacenter) si.getSearchIndex().findByInventoryPath(datacenterList.get(0));
			String currentDcId= dc.getMOR().getVal();
			int eventCount = 0;

			for (int i=0; i<hostList.size(); i++) {
				String runTime = "runTime";
				if(i==0) {
					runTime = "FirstRun";
				} 
	
				HostSystem host = (HostSystem) new InventoryNavigator(rootFolder).searchManagedEntity("HostSystem", hostList.get(i));
				HostNetworkSystem hns = host.getHostNetworkSystem();

				for (int j=0; j<addpgList.size(); j++) {

					ArrayList<String> addpgVSSList = addpgMap.get(addpgList.get(j));
					for (int k=0; k<addpgVSSList.size(); k++) {

						if(delayFlag && eventCount != 0 && eventCount %3 == 0) {
							Thread.sleep(11000);
						}
						eventCount++;

						HostPortGroupSpec pg = new HostPortGroupSpec();
						pg.setName(addpgList.get(j));
						pg.setVswitchName(addpgVSSList.get(k));
						pg.setPolicy(new HostNetworkPolicy());
						PrintMsg.ItemPrint("Adding PG: " + addpgList.get(j) + " In to vSwitch: " + addpgVSSList.get(k) + " In Host: " + hostList.get(i)) ;
						hns.addPortGroup(pg);
						if (runTime.equals("FirstRun")) {
							bw.write(currentDcId + "_" + addpgList.get(j) +  "\n");
						}
					}
		
				}
			}
			bw.close();

		} catch (Exception e) {
			PrintMsg.ErrorPrint("Error Creating PGs: " + e);
			finalStatus = "FAIL";
			return finalStatus;
		}

		return finalStatus;

	}




	public static String DeletePGs(ServiceInstance si, ArrayList<String> hostList, ArrayList<String> addpgList, ArrayList<String> datacenterList, boolean delayFlag) throws Exception { 

		PrintMsg.HeaderPrint("NetworkOps Command Execution: DeletePGs");
		String finalStatus = "SUCCESS";

		String fileName = "./RunOutput/UpdatePGDelete_"+datacenterList.get(0)+"PortGroup.txt";
		BufferedWriter bw = writeFile(fileName);
		int eventCount = 0;
	
		try {

			Folder rootFolder = si.getRootFolder();
			Datacenter dc = (Datacenter) si.getSearchIndex().findByInventoryPath(datacenterList.get(0));
			String currentDcId= dc.getMOR().getVal();

			for (int i=0; i<hostList.size(); i++) {
				String runTime = "runTime";
				if(i==0) {
					runTime = "FirstRun";
				} 
				HostSystem host = (HostSystem) new InventoryNavigator(rootFolder).searchManagedEntity("HostSystem", hostList.get(i));
				HostNetworkSystem hns = host.getHostNetworkSystem();

				for (int j=0; j<addpgList.size(); j++) {

					if(delayFlag && eventCount != 0 && eventCount %3 == 0) {
						Thread.sleep(11000);
					}
					eventCount++;

					hns.removePortGroup(addpgList.get(j));
					PrintMsg.ItemPrint("Deleting PG: " + addpgList.get(j) + " From Host: " + hostList.get(i)) ;

					if (runTime.equals("FirstRun")) {
						bw.write(currentDcId + "_" + addpgList.get(j) +  "\n");
					}
				}
				
			}
			bw.close();

		} catch (Exception e) {
			PrintMsg.ErrorPrint("Error Deleting PGs: " + e);
			finalStatus = "FAIL";
			return finalStatus;
		}

		return finalStatus;

	}


	public static String AddDvPGs(ServiceInstance si, ArrayList<String> adddvpgList, Map<String, ArrayList<String>> adddvpgMap, ArrayList<String> datacenterList, boolean delayFlag) throws Exception { 

		PrintMsg.HeaderPrint("NetworkOps Command Execution: AddDvPGs");
		String finalStatus = "SUCCESS";

		String fileName = "./RunOutput/UpdateDvPGAdd_"+datacenterList.get(0)+"PortGroup.txt";
		BufferedWriter bw = writeFile(fileName);
	
		try {

			Folder rootFolder = si.getRootFolder();
			Datacenter dc = (Datacenter) si.getSearchIndex().findByInventoryPath(datacenterList.get(0));
			String currentDcId= dc.getMOR().getVal();

			int eventCount = 0;

			for (int i=0; i<adddvpgList.size(); i++) {
				ArrayList<String> adddvpgDVSList = adddvpgMap.get(adddvpgList.get(i));
				for (int j=0; j<adddvpgDVSList.size(); j++) {

					if(delayFlag && eventCount != 0 && eventCount %3 == 0) {
						Thread.sleep(11000);
					}
					eventCount++;

					VmwareDistributedVirtualSwitch dvs = (VmwareDistributedVirtualSwitch) new InventoryNavigator(rootFolder).searchManagedEntity("DistributedVirtualSwitch", adddvpgDVSList.get(j));
					DVPortgroupConfigSpec[] dvpgs = new DVPortgroupConfigSpec[1];
					dvpgs[0] = new DVPortgroupConfigSpec();
					dvpgs[0].setName(adddvpgList.get(i));
					dvpgs[0].setNumPorts(128);
					dvpgs[0].setType("earlyBinding");
					VMwareDVSPortSetting vport = new VMwareDVSPortSetting();
					dvpgs[0].setDefaultPortConfig(vport);
					VmwareDistributedVirtualSwitchVlanIdSpec vlan = new VmwareDistributedVirtualSwitchVlanIdSpec();
					vport.setVlan(vlan);
					vlan.setInherited(false);
					vlan.setVlanId(0);
	
					PrintMsg.ItemPrint("Adding DvPG: " + adddvpgList.get(i) + " In to dvSwitch: " + adddvpgDVSList.get(j));
					Task task = dvs.addDVPortgroup_Task(dvpgs);
			        	String status = task.waitForTask();
					if (!status.equals("success")) {
						PrintMsg.ErrorPrint("Error Adding DvPG : " + adddvpgList.get(i));
						finalStatus = "FAIL";
						return finalStatus;
					}

				}
				bw.write(currentDcId + "_" + adddvpgList.get(i) +  "\n");
		
			}
			bw.close();

		} catch (Exception e) {
			PrintMsg.ErrorPrint("Error Creating PGs: " + e);
			finalStatus = "FAIL";
			return finalStatus;
		}

		return finalStatus;

	}

	public static String DeleteDvPGs(ServiceInstance si, ArrayList<String> adddvpgList, ArrayList<String> datacenterList, boolean delayFlag) throws Exception { 

		PrintMsg.HeaderPrint("NetworkOps Command Execution: DeleteDvPGs");
		String finalStatus = "SUCCESS";

		String fileName = "./RunOutput/UpdateDvPGDelete_"+datacenterList.get(0)+"PortGroup.txt";
		BufferedWriter bw = writeFile(fileName);
		int eventCount = 0;
	
		try {

			Folder rootFolder = si.getRootFolder();
			Datacenter dc = (Datacenter) si.getSearchIndex().findByInventoryPath(datacenterList.get(0));
			String currentDcId= dc.getMOR().getVal();

			for (int i=0; i<adddvpgList.size(); i++) {
				DistributedVirtualPortgroup dvpg = (DistributedVirtualPortgroup) new InventoryNavigator(rootFolder).searchManagedEntity("DistributedVirtualPortgroup", adddvpgList.get(i));

				if(delayFlag && eventCount != 0 && eventCount %3 == 0) {
					Thread.sleep(11000);
				}
				eventCount++;

				Task task = dvpg.destroy_Task();

				PrintMsg.ItemPrint("Deleting DvPG : " + adddvpgList.get(i));
			        String status = task.waitForTask();
				if (!status.equals("success")) {
					PrintMsg.ErrorPrint("Error Deleting DvPG : " + adddvpgList.get(i));
					finalStatus = "FAIL";
					return finalStatus;
				}

				bw.write(currentDcId + "_" + adddvpgList.get(i) +  "\n");
		
			}
			bw.close();

		} catch (Exception e) {
			PrintMsg.ErrorPrint("Error Deleting DvPGs: " + e);
			finalStatus = "FAIL";
			return finalStatus;
		}

		return finalStatus;

	}


	public static String AddDvPGVlans(ServiceInstance si, ArrayList<String> adddvpgvlanList, ArrayList<String> adddvpgList, ArrayList<String> datacenterList ) throws Exception { 

		PrintMsg.HeaderPrint("NetworkOps Command Execution: AddDvPGVlans");
		String finalStatus = "SUCCESS";

		String fileName = "./RunOutput/UpdateDvPGVlan_"+datacenterList.get(0)+"PortGroup.txt";
		BufferedWriter bw = writeFile(fileName);
	
		try {

			Folder rootFolder = si.getRootFolder();
			Datacenter dc = (Datacenter) si.getSearchIndex().findByInventoryPath(datacenterList.get(0));
			String currentDcId= dc.getMOR().getVal();

			for (int i=0; i<adddvpgList.size(); i++) {
				DistributedVirtualPortgroup dvpg = (DistributedVirtualPortgroup) new InventoryNavigator(rootFolder).searchManagedEntity("DistributedVirtualPortgroup", adddvpgList.get(i));
				String version = dvpg.getConfig().configVersion; 
				DVPortgroupConfigSpec dvpgSpec = new DVPortgroupConfigSpec();
				dvpgSpec.configVersion = version;
				VMwareDVSPortSetting vport = new VMwareDVSPortSetting();
				dvpgSpec.setDefaultPortConfig(vport);

				if(adddvpgvlanList.size() == 1 && !(adddvpgvlanList.get(0).contains("-"))) { // DvPG is going to put in VST mode
					VmwareDistributedVirtualSwitchVlanIdSpec vlanSpec = new VmwareDistributedVirtualSwitchVlanIdSpec();
					vport.setVlan(vlanSpec);
					vlanSpec.setInherited(false);
					PrintMsg.ItemPrint("Adding VLAN: " + adddvpgvlanList.get(0) + " Into DvPG: " + adddvpgList.get(i));
					vlanSpec.setVlanId(Integer.parseInt(adddvpgvlanList.get(0)));


				}else { // DvPG is going to put in VGT mode
					VmwareDistributedVirtualSwitchTrunkVlanSpec vlanTrunkSpec = new VmwareDistributedVirtualSwitchTrunkVlanSpec();
					vport.setVlan(vlanTrunkSpec);
					vlanTrunkSpec.setInherited(false);

					NumericRange[] vlanIdRanges = new NumericRange[adddvpgvlanList.size()]; NumericRange currenvlanId = new NumericRange();					
					
					for(int j=0; j<adddvpgvlanList.size(); j++) {
						NumericRange currentvlanId = new NumericRange();
						vlanIdRanges[j] = currentvlanId;

						String currentInt = adddvpgvlanList.get(j);
						PrintMsg.ItemPrint("Adding VLAN Range: " + currentInt + " Into DvPG: " + adddvpgList.get(i));
						if(currentInt.contains("-")) {
							String[] vlanInts = currentInt.split("[-]");
							currentvlanId.start = Integer.parseInt(vlanInts[0]);
							currentvlanId.end = Integer.parseInt(vlanInts[1]);
						}else {
							currentvlanId.start = Integer.parseInt(currentInt);
							currentvlanId.end = Integer.parseInt(currentInt);
						}
					}
					vlanTrunkSpec.setVlanId(vlanIdRanges);
				}

				Task task = dvpg.reconfigureDVPortgroup_Task(dvpgSpec);
				String status = task.waitForTask();
				if (!status.equals("success")) {
					PrintMsg.ErrorPrint("Error Adding VLAN: " + adddvpgvlanList.get(i));
					finalStatus = "FAIL";
					return finalStatus;
				}
				bw.write(currentDcId + "_" + adddvpgList.get(i) +  "\t");
				for(int k=0; k<adddvpgvlanList.size(); k++) {
					bw.write(adddvpgvlanList.get(k) + "\t");
				}
				bw.write("\n");
			}
			bw.close();

		} catch (Exception e) {
			PrintMsg.ErrorPrint("Error Adding VLANs To DvPGs: " + e);
			finalStatus = "FAIL";
			return finalStatus;
		}

		return finalStatus;

	}



	public static String AddPGVlans(ServiceInstance si, ArrayList<String> hostList, ArrayList<Integer> addpgvlanList, ArrayList<String> addpgList, Map<String, ArrayList<String>> addpgMap, ArrayList<String> datacenterList ) throws Exception { 

		PrintMsg.HeaderPrint("NetworkOps Command Execution: AddPGVlans");
		String finalStatus = "SUCCESS";

		String fileName = "./RunOutput/UpdatePGVlan_"+datacenterList.get(0)+"PortGroup.txt";
		BufferedWriter bw = writeFile(fileName);
	
		try {

			Folder rootFolder = si.getRootFolder();
			Datacenter dc = (Datacenter) si.getSearchIndex().findByInventoryPath(datacenterList.get(0));
			String currentDcId= dc.getMOR().getVal();

			for (int i=0; i<hostList.size(); i++) {
				String runTime = "runTime";
				if(i==0) {
					runTime = "FirstRun";
				} 
	
				HostSystem host = (HostSystem) new InventoryNavigator(rootFolder).searchManagedEntity("HostSystem", hostList.get(i));
				HostNetworkSystem hns = host.getHostNetworkSystem();

				for (int j=0; j<addpgList.size(); j++) {
					ArrayList<String> addpgVSSList = addpgMap.get(addpgList.get(j));
					for (int k=0; k<addpgVSSList.size(); k++) {
						HostPortGroupSpec pg = new HostPortGroupSpec();
						pg.setName(addpgList.get(j));
						//pg.setVlanId(addpgvlanList.get(0));
						pg.setVlanId(addpgvlanList.get(j));
						pg.setVswitchName(addpgVSSList.get(k));

						pg.setPolicy(new HostNetworkPolicy());
						PrintMsg.ItemPrint("Adding VLAN: " + addpgvlanList.get(j) + " Into PG: " + addpgList.get(j) + " In vSwitch: " + addpgVSSList.get(k) + " In Host: " + hostList.get(i)) ;
						hns.updatePortGroup(addpgList.get(j), pg);
						if (runTime.equals("FirstRun")) {
							bw.write(currentDcId + "_" + addpgList.get(j) +  "\t");
							bw.write(addpgvlanList.get(0) +  "\n");
						}
					}
		
				}
			}
			bw.close();
	
		} catch (Exception e) {
			PrintMsg.ErrorPrint("Error Adding VLANs To PGs: " + e);
			finalStatus = "FAIL";
			return finalStatus;
		}

		return finalStatus;




	}


	public static String AddStdVMks(ServiceInstance si, ArrayList<String> hostList, ArrayList<String> vswitchList, Map<String, ArrayList<String>> addstdvmkMap, Map<String, ArrayList<String>> addpgMap, ArrayList<String> datacenterList, boolean delayFlag ) throws Exception { 

		PrintMsg.HeaderPrint("NetworkOps Command Execution: AddStdVMks");
		String finalStatus = "SUCCESS";

		String pgFileName = "./RunOutput/UpdateVMKAdd_"+datacenterList.get(0)+"PortGroup.txt";
		String macFileName = "./RunOutput/VMkMAC_ADD_"+datacenterList.get(0)+".txt";
		BufferedWriter bw_pgFileName = writeFile(pgFileName);
		BufferedWriter bw_macFileName = writeFile(macFileName);
	
		try {

			Folder rootFolder = si.getRootFolder();
			Datacenter dc = (Datacenter) si.getSearchIndex().findByInventoryPath(datacenterList.get(0));
			String currentDcId= dc.getMOR().getVal();
			int eventCount = 0;

			int lastIPByte = 1;
			for (int i=0; i<hostList.size(); i++) {
				String runTime = "runTime";
				if(i==0) {
					runTime = "FirstRun";
				} 
	
				HostSystem host = (HostSystem) new InventoryNavigator(rootFolder).searchManagedEntity("HostSystem", hostList.get(i));
				HostNetworkSystem hns = host.getHostNetworkSystem();

				for (int k=0; k<vswitchList.size(); k++) {
					ArrayList<String> addvmkList = addstdvmkMap.get(hostList.get(i).concat(vswitchList.get(k)));
					for (int j=0; j<addvmkList.size(); j++) {

						if(delayFlag && eventCount != 0 && eventCount %3 == 0) {
							Thread.sleep(11000);
						}
						eventCount++;

						PrintMsg.ItemPrint("Adding VMKernel: " + addvmkList.get(j) + " In to vSwitch: " + vswitchList.get(k) + " In Host: " + hostList.get(i)) ;
						HostPortGroupSpec pg = new HostPortGroupSpec();
						pg.setName(addvmkList.get(j));
						pg.setVswitchName(vswitchList.get(k));
						pg.setPolicy(new HostNetworkPolicy());
						hns.addPortGroup(pg);

						HostVirtualNicSpec hvns = new HostVirtualNicSpec();
						HostIpConfig hic = new HostIpConfig();
						hic.setDhcp(false);
						String IPAdd = "101.101.101.";
						String currentIP = IPAdd.concat(new Integer(lastIPByte).toString());
						lastIPByte++;
						hic.setIpAddress(currentIP);
						hic.setSubnetMask("255.255.255.0");
						hvns.setIp(hic);
						String result = hns.addVirtualNic(addvmkList.get(j), hvns);

						if (runTime.equals("FirstRun")) {
							bw_pgFileName.write(currentDcId + "_" + addvmkList.get(j) +  "\n");
						}
					}
		
				}
				for (int j=0; j<vswitchList.size(); j++) {
					ArrayList<String> addvmkList = addstdvmkMap.get(hostList.get(i).concat(vswitchList.get(j)));
					for (int k=0; k<addvmkList.size(); k++) {
						HostVirtualNicConfig[] hvnicConfigs = hns.getNetworkConfig().getVnic();
						for (int m=0; m<hvnicConfigs.length; m++) {
							HostVirtualNicConfig hvnicCfg = hvnicConfigs[m];
							String pg = hvnicCfg.getSpec().getPortgroup();
							if(pg != null && pg.equals(addvmkList.get(k))) {
								bw_macFileName.write(pg + "\t");
								bw_macFileName.write(hvnicCfg.getSpec().getMac() + "\n");
							}
						}
					}
				}

			}
			bw_macFileName.close();
			bw_pgFileName.close();

		} catch (Exception e) {
			PrintMsg.ErrorPrint("Error Creating Std VMkernels: " + e);
			finalStatus = "FAIL";
			return finalStatus;
		}

		return finalStatus;

	}



	public static String DeleteStdVMks(ServiceInstance si, ArrayList<String> hostList, ArrayList<String> vswitchList, Map<String, ArrayList<String>> addstdvmkMap, ArrayList<String> datacenterList ) throws Exception { 

		PrintMsg.HeaderPrint("NetworkOps Command Execution: DeleteStdVMK");
		String finalStatus = "SUCCESS";

		String pgFileName = "./RunOutput/UpdateVMKDel_"+datacenterList.get(0)+"PortGroup.txt";
		String macFileName = "./RunOutput/VMkMAC_DEL_"+datacenterList.get(0)+".txt";
		BufferedWriter bw_pgFileName = writeFile(pgFileName);
		BufferedWriter bw_macFileName = writeFile(macFileName);
	
	
		try {

			Folder rootFolder = si.getRootFolder();
			Datacenter dc = (Datacenter) si.getSearchIndex().findByInventoryPath(datacenterList.get(0));
			String currentDcId= dc.getMOR().getVal();

			for (int i=0; i<hostList.size(); i++) {
				String runTime = "runTime";
				if(i==0) {
					runTime = "FirstRun";
				} 
	
				HostSystem host = (HostSystem) new InventoryNavigator(rootFolder).searchManagedEntity("HostSystem", hostList.get(i));
				HostNetworkSystem hns = host.getHostNetworkSystem();

				for (int j=0; j<vswitchList.size(); j++) {
					ArrayList<String> addvmkList = addstdvmkMap.get(hostList.get(i).concat(vswitchList.get(j)));
					for (int k=0; k<addvmkList.size(); k++) {
						HostVirtualNicConfig[] hvnicConfigs = hns.getNetworkConfig().getVnic();
						for (int m=0; m<hvnicConfigs.length; m++) {
							HostVirtualNicConfig hvnicCfg = hvnicConfigs[m];
							String pg = hvnicCfg.getSpec().getPortgroup();
							if(pg != null && pg.equals(addvmkList.get(k))) {
								bw_macFileName.write(pg + "\t");
								bw_macFileName.write(hvnicCfg.getSpec().getMac() + "\n");
							}
						}
					}

					HostVirtualNic[] hvns = hns.getNetworkInfo().getVnic();
					for (int m=0; m<addvmkList.size(); m++) {
						for(int k=0; k<hvns.length; k++) {
							HostVirtualNic nic = hvns[k];
							String pg = nic.getPortgroup();
							if(pg != null && pg.equals(addvmkList.get(m))){
								PrintMsg.ItemPrint("Deleting Standard VMKernel: " + addvmkList.get(m) + " From Host: " + hostList.get(i)) ;
								hns.removeVirtualNic(nic.getDevice());
							}
						}
					}

					for (int m=0; m<addvmkList.size(); m++) {
						hns.removePortGroup(addvmkList.get(m));
						if (runTime.equals("FirstRun")) {
							bw_pgFileName.write(currentDcId + "_" + addvmkList.get(m) +  "\n");
						}
					}

				}
			}
			bw_macFileName.close();
			bw_pgFileName.close();

		} catch (Exception e) {
			PrintMsg.ErrorPrint("Error Deleting Std VMkernels: " + e);
			finalStatus = "FAIL";
			return finalStatus;
		}

		return finalStatus;

	}




}



