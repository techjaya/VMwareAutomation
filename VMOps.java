//@author Don Jayakody
package vCenterOps;

import java.net.URL;
import java.util.*;
import java.io.*;
import com.vmware.vim25.*;
import com.vmware.vim25.mo.*;


public class VMOps {
	
	public static String CloneVM(ServiceInstance si, Map<String, ArrayList<String>> vmMap, Map<String, ArrayList<String>> clonevmMap, ArrayList<String> hostList, ArrayList<String>dvswitchList, Map<String, ArrayList<String>> dvpgMap, ArrayList<String> datacenterList, boolean delayFlag) throws Exception {

		PrintMsg.HeaderPrint("VMOps Command Execution: CloneVM");
		String finalStatus = "SUCCESS";
		String fileName = "./RunOutput/VMMAC_CLONE_"+datacenterList.get(0)+".txt";
		BufferedWriter bw = writeFile(fileName);

		Folder rootFolder = si.getRootFolder();
		int eventCount = 0;

		for (int k=0; k<hostList.size(); k++) { 

			ArrayList<String> vmList = vmMap.get(hostList.get(k));
			ArrayList<String> clonevmList = clonevmMap.get(hostList.get(k));

			for (int i=0; i<vmList.size(); i++ ) {
				VirtualMachine vm = (VirtualMachine) new InventoryNavigator(rootFolder).searchManagedEntity("VirtualMachine", vmList.get(i));

				if(vm==null) {	
					PrintMsg.ErrorPrint("CloneVM: VM " + vmList.get(i) + " not found");
					finalStatus = "FAIL";
					return finalStatus;
				}
				
				VirtualMachineCloneSpec cloneSpec = new VirtualMachineCloneSpec();
				cloneSpec.setLocation(new VirtualMachineRelocateSpec());
				cloneSpec.setPowerOn(false);
				cloneSpec.setTemplate(false);
		

				if(delayFlag && eventCount != 0 && eventCount %3 == 0) {
					Thread.sleep(11000);
				}
				eventCount++;

				PrintMsg.ItemPrint("Cloing " + vmList.get(i) + " into " + clonevmList.get(i));		
				Task task = vm.cloneVM_Task((Folder) vm.getParent(), clonevmList.get(i), cloneSpec);
				
				
				String status = task.waitForTask();
				if (!status.equals("success")) {
					PrintMsg.ErrorPrint("Error Cloning VM : "+ vmList.get(i));
					finalStatus = "FAIL";
					return finalStatus;

				
				//if the task is successful write the newly cloned vm mac addresses to the "vmmacadd.txt"
				}else {
					VirtualMachine clonedVM = (VirtualMachine) new InventoryNavigator(rootFolder).searchManagedEntity("VirtualMachine", clonevmList.get(i));
					writeVMMACstoFile(rootFolder, clonevmList.get(i), clonedVM, dvswitchList, dvpgMap, bw);
				}

			}
		}
		bw.close();
		return  finalStatus;
	
	}
	

		public static String CreateVM(ServiceInstance si, ArrayList<String> datacenterList, ArrayList<String> datastoreList, Map<String, ArrayList<String>> vmMap, ArrayList<String> stdvnicList, ArrayList<String> dvnicList, ArrayList<String> hostList, ArrayList<String> dvswitchList, Map<String, ArrayList<String>>dvpgMap, boolean delayFlag) throws Exception {
		
		PrintMsg.HeaderPrint("VMOps Command Execution: CreateVM");
		String finalStatus = "SUCCESS";
		
		//String fileName = "./RunOutput/VMMAC_CREATE.txt";
		String fileName = "./RunOutput/VMMAC_CREATE_"+datacenterList.get(0)+".txt";
		BufferedWriter bw = writeFile(fileName);
		int eventCount = 0;

		try {
			Folder rootFolder = si.getRootFolder();
 
			for(int i=0; i<datacenterList.size(); i++) {
				
				for (int j=0; j<hostList.size(); j++) {	


					Datacenter dc = (Datacenter) new InventoryNavigator(rootFolder).searchManagedEntity("Datacenter", datacenterList.get(i));
					Folder vmFolder = dc.getVmFolder();

					ArrayList<String> vmList = vmMap.get(hostList.get(j));

					String nicName = "Network Adapter ";	

					for(int k=0; k<vmList.size(); k++) {

						VirtualMachineConfigSpec vmSpec = new VirtualMachineConfigSpec();
						vmSpec.setName(vmList.get(k));
						
						//Add stdvnics to the vm
						int fullListSize = stdvnicList.size() + dvnicList.size();
						VirtualDeviceConfigSpec[] nicSpecArray = new VirtualDeviceConfigSpec[fullListSize];
						for (int m=0; m<stdvnicList.size(); m++) {
							String currentNic = nicName + Integer.toString(m+1);
							String netName = stdvnicList.get(m);
							VirtualDeviceConfigSpec nicSpec = new VirtualDeviceConfigSpec();
							nicSpec.setOperation(VirtualDeviceConfigSpecOperation.add);
							VirtualEthernetCard nic = new VirtualPCNet32();
							VirtualEthernetCardNetworkBackingInfo nicBacking = new VirtualEthernetCardNetworkBackingInfo();
							nicBacking.setDeviceName(netName);
							Description info = new Description();
							info.setLabel(currentNic);
							info.setSummary(netName);
							nic.setDeviceInfo(info);
							nic.setAddressType("assigned");
							nic.setBacking(nicBacking);
							nic.setKey(0);
							nicSpec.setDevice(nic);
							nicSpecArray[m] = nicSpec;
						}
						//Add dvnics to the vm
						boolean gotDVNIC = false;
						for (int b=0; b<dvnicList.size(); b++ ) {
							for(int c=0; c<dvswitchList.size(); c++) {
								gotDVNIC = false;
								ArrayList<String> dvpgList = dvpgMap.get(dvswitchList.get(c));
								for(int d=0; d<dvpgList.size(); d++) {
									
									if(dvpgList.get(d).equals(dvnicList.get(b))) {
										DistributedVirtualPortgroup dvpg = (DistributedVirtualPortgroup) new InventoryNavigator(rootFolder).searchManagedEntity("DistributedVirtualPortgroup", dvnicList.get(b));
										VmwareDistributedVirtualSwitch dvs = (VmwareDistributedVirtualSwitch) new InventoryNavigator(rootFolder).searchManagedEntity("DistributedVirtualSwitch", dvswitchList.get(c)); 
										VirtualDeviceConfigSpec nicSpec = new VirtualDeviceConfigSpec();

										nicSpec.setOperation(VirtualDeviceConfigSpecOperation.add);
										VirtualEthernetCard nic = new VirtualPCNet32();
										VirtualEthernetCardDistributedVirtualPortBackingInfo nicBacking = new VirtualEthernetCardDistributedVirtualPortBackingInfo();
										DistributedVirtualSwitchPortConnection portConnection = new DistributedVirtualSwitchPortConnection();
										portConnection.setPortgroupKey(dvpg.getKey());
										portConnection.setSwitchUuid(dvs.getSummary().getUuid());
										nicBacking.setPort(portConnection);
										nic.setAddressType("generated");
										nic.setBacking(nicBacking);
										nicSpec.setDevice(nic);
										int arrayIndex = b+stdvnicList.size();
										nicSpecArray[arrayIndex] = nicSpec;
										gotDVNIC = true;
										break;
									}
								}
								if(gotDVNIC) break;
							}
						}
						vmSpec.setDeviceChange(nicSpecArray);

						VirtualMachineFileInfo vmfi = new VirtualMachineFileInfo();
						vmfi.setVmPathName("["+datastoreList.get(j) +"]");
						vmSpec.setFiles(vmfi);

						HostSystem host = (HostSystem) new InventoryNavigator(rootFolder).searchManagedEntity("HostSystem", hostList.get(j));
						ComputeResource cr = (ComputeResource)host.getParent();

						if(delayFlag && eventCount != 0 && eventCount %3 == 0) {
							Thread.sleep(11000);
						}
						eventCount++;

						Task task = vmFolder.createVM_Task(vmSpec, cr.getResourcePool(), null);

						PrintMsg.ItemPrint("Creating VM: " + vmList.get(k));

						String status = task.waitForTask();
						if (!status.equals("success")) {
							PrintMsg.ErrorPrint("Error Creating VM : " + vmList.get(k));
							finalStatus = "FAIL";
							return finalStatus;
						}else {
							VirtualMachine createdVM = (VirtualMachine) new InventoryNavigator(rootFolder).searchManagedEntity("VirtualMachine", vmList.get(k));
							writeVMMACstoFile(rootFolder, vmList.get(k),createdVM, dvswitchList, dvpgMap, bw);

						}

					}
				}
			}
			bw.close();
		} catch (Exception e) {
			PrintMsg.ErrorPrint("Error Creating VMs: " + e);
			e.printStackTrace();
			finalStatus = "FAIL";
			return finalStatus;
		}
	
		return finalStatus; 

	}
	
	public static String RenameVM(ServiceInstance si, Map<String, ArrayList<String>> vmMap, Map<String, ArrayList<String>> renamevmMap, ArrayList<String> hostList, ArrayList<String> dvswitchList, Map<String, ArrayList<String>> dvpgMap, ArrayList<String> datacenterList, boolean delayFlag) throws Exception {	

		PrintMsg.HeaderPrint("VMOps Command Execution: RenameVM");
		String finalStatus = "SUCCESS";
		String fileName = "./RunOutput/VMMAC_RENAME_"+datacenterList.get(0)+".txt";
		BufferedWriter bw = writeFile(fileName);
		int eventCount = 0;


		try {
			Folder rootFolder = si.getRootFolder();
			
			for(int a=0; a<hostList.size(); a++) {

				ArrayList<String> vmList = vmMap.get(hostList.get(a)); 
				ArrayList<String> renamevmList = renamevmMap.get(hostList.get(a)); 
				
				for (int i=0; i<vmList.size(); i++) {

					VirtualMachine vm = (VirtualMachine) new InventoryNavigator(rootFolder).searchManagedEntity("VirtualMachine", vmList.get(i));
					if(vm==null) {	
						PrintMsg.ErrorPrint("RenameVM: VM " + vmList.get(i) + " not found");
						finalStatus = "FAIL";
						return finalStatus;
					}
			
					//Before renaming the vm retrieve the MAC address information	
					writeVMMACstoFile(rootFolder, renamevmList.get(i),vm, dvswitchList, dvpgMap, bw);


					if(delayFlag && eventCount != 0 && eventCount %3 == 0) {
						Thread.sleep(11000);
					}
					eventCount++;

					Task task = vm.rename_Task(renamevmList.get(i));
					PrintMsg.ItemPrint("Renaming VM: " + vmList.get(i) + " into " + renamevmList.get(i));
					String status = task.waitForTask();
					if (!status.equals("success")) {
						PrintMsg.ErrorPrint("Error Renaming VM : " + vmList.get(i));
						finalStatus = "FAIL";
						return finalStatus;
					}
			
				}	

			}
			bw.close();

		} catch (Exception e) {
			PrintMsg.ErrorPrint("Error Renaming VMs: " + e);
			finalStatus = "FAIL";
			return finalStatus;
		}
	
		return finalStatus;

	}

	public static String DeleteVM(ServiceInstance si, Map<String, ArrayList<String>> deletevmMap, ArrayList<String> hostList, ArrayList<String> dvswitchList, Map<String, ArrayList<String>> dvpgMap, ArrayList<String> datacenterList, boolean delayFlag) throws Exception { 

	 	PrintMsg.HeaderPrint("VMOps Command Execution: DeleteVM");
		String finalStatus = "SUCCESS";
		String fileName = "./RunOutput/VMMAC_DELETE_"+datacenterList.get(0)+".txt";
		BufferedWriter bw = writeFile(fileName);
		int eventCount = 0;

		try {
			for (int a=0; a<hostList.size(); a++) {

				ArrayList<String> deletevmList = deletevmMap.get(hostList.get(a)); 

				Folder rootFolder = si.getRootFolder();
				for (int i=0; i<deletevmList.size(); i++) {

					VirtualMachine vm = (VirtualMachine) new InventoryNavigator(rootFolder).searchManagedEntity("VirtualMachine", deletevmList.get(i));
					if(vm==null) {	
						//Skip it
					} else {
						//Before deleting the vm retrieve the MAC address information	
						writeVMMACstoFile(rootFolder, deletevmList.get(i), vm, dvswitchList, dvpgMap, bw);

						if(delayFlag && eventCount != 0 && eventCount %3 == 0) {
							Thread.sleep(11000);
						}
						eventCount++;

						Task task = vm.destroy_Task();
						PrintMsg.ItemPrint("Deleting VM : " + deletevmList.get(i));
						String status = task.waitForTask();
						if (!status.equals("success")) {
							PrintMsg.ErrorPrint("Error Deleting VM : " + deletevmList.get(i));
							finalStatus = "FAIL";
							return finalStatus;
						}
					}

				}
			}
			bw.close();

		} catch (Exception e) {
			PrintMsg.ErrorPrint("Error Deleting VMs: " + e);
			finalStatus = "FAIL";
			return finalStatus;
		}
	

		return finalStatus;
	}


	public static String AddStdVnic(ServiceInstance si, Map<String, ArrayList<String>> deletevmMap, ArrayList<String> hostList, ArrayList<String> addpgList, ArrayList<String> dvswitchList, Map<String, ArrayList<String>> dvpgMap, ArrayList<String> datacenterList) throws Exception { 

	 	PrintMsg.HeaderPrint("VMOps Command Execution: AddStdVnic");
		String finalStatus = "SUCCESS";
		String fileName = "./RunOutput/VMMAC_ADDStdVnic_"+datacenterList.get(0)+".txt";
		BufferedWriter bw = writeFile(fileName);

		try {

			Folder rootFolder = si.getRootFolder();
			PrintMsg.MsgPrint("Adding Following vNICS : ");
			for (int e=0; e<addpgList.size(); e++) {
				PrintMsg.ItemPrint(addpgList.get(e));
			}

			for (int a=0; a<hostList.size(); a++) {
				ArrayList<String> vmList = deletevmMap.get(hostList.get(a)); 
				
				for (int i=0; i<vmList.size(); i++) {

					VirtualMachine vm = (VirtualMachine) new InventoryNavigator(rootFolder).searchManagedEntity("VirtualMachine", vmList.get(i));
					if(vm!=null) {
			
						VirtualMachineConfigSpec vmConfigSpec = new VirtualMachineConfigSpec();

						VirtualDeviceConfigSpec[] nicSpecArray = new VirtualDeviceConfigSpec[addpgList.size()];
						for (int j=0; j<addpgList.size(); j++) {
							VirtualDeviceConfigSpec nicSpec = new VirtualDeviceConfigSpec();
							VirtualMachineConfigInfo vmConfigInfo = vm.getConfig();

							nicSpec.setOperation(VirtualDeviceConfigSpecOperation.add);
							VirtualEthernetCard nic = new VirtualPCNet32();
							VirtualEthernetCardNetworkBackingInfo nicBacking = new VirtualEthernetCardNetworkBackingInfo();
							nicBacking.setDeviceName(addpgList.get(j));
							nic.setAddressType("generated");
							nic.setBacking(nicBacking);
							nic.setKey(4);
							nicSpec.setDevice(nic);
							nicSpecArray[j] = nicSpec;

						}
						vmConfigSpec.setDeviceChange(nicSpecArray);

						Task task = vm.reconfigVM_Task(vmConfigSpec);
						PrintMsg.ItemPrint("Adding " + addpgList.size() + " vNics to  VM : " + vmList.get(i));
						String status = task.waitForTask();
						if (!status.equals("success")) {
							PrintMsg.ErrorPrint("Error Adding vNic VM : " + vmList.get(i));
							finalStatus = "FAIL";
							return finalStatus;
						}else {
							//Before renaming the vm retrieve the MAC address information	
							VirtualMachine reconfigVM = (VirtualMachine) new InventoryNavigator(rootFolder).searchManagedEntity("VirtualMachine", vmList.get(i));
							writeVMMACstoFile(rootFolder, vmList.get(i), reconfigVM, dvswitchList, dvpgMap, bw);
						}
					}

				}
			}
			bw.close();

		} catch (Exception e) {
			PrintMsg.ErrorPrint("Error Adding VNICs: " + e);
			finalStatus = "FAIL";
			return finalStatus;
		}
	

		return finalStatus;
	}


	public static String DeleteStdVnic(ServiceInstance si, Map<String, ArrayList<String>> deletevmMap, ArrayList<String> hostList, ArrayList<String> addpgList, ArrayList<String> dvswitchList, Map<String, ArrayList<String>> dvpgMap, ArrayList<String> datacenterList) throws Exception { 

	 	PrintMsg.HeaderPrint("VMOps Command Execution: DeleteStdVnic");
		String finalStatus = "SUCCESS";
		String fileName_deletedMACs = "./RunOutput/VMMAC_DELStdVnicDeletedMACs_"+datacenterList.get(0)+".txt";
		String fileName_remainingMACs = "./RunOutput/VMMAC_DELStdVnicRemainingMACs_"+datacenterList.get(0)+".txt";

		BufferedWriter bw_deletedMACs = writeFile(fileName_deletedMACs);
		BufferedWriter bw_remainingMACs = writeFile(fileName_remainingMACs);

		try {

			Folder rootFolder = si.getRootFolder();
			PrintMsg.MsgPrint("Deleting Following vNICS : ");
			for (int e=0; e<addpgList.size(); e++) {
				PrintMsg.ItemPrint(addpgList.get(e));
			}


			boolean newFile = true;
			for (int a=0; a<hostList.size(); a++) {
	
				ArrayList<String> vmList = deletevmMap.get(hostList.get(a)); 
				
				for (int i=0; i<vmList.size(); i++) {

					VirtualMachine vm = (VirtualMachine) new InventoryNavigator(rootFolder).searchManagedEntity("VirtualMachine", vmList.get(i));
					if (vm != null) {
						if(!newFile) {
							bw_deletedMACs.write("\n");
						}
						newFile = false;

						VirtualMachineConfigSpec vmConfigSpec = new VirtualMachineConfigSpec();
						VirtualDeviceConfigSpec[] nicSpecArray = new VirtualDeviceConfigSpec[addpgList.size()];
						boolean sameVM = false;
						for (int j=0; j<addpgList.size(); j++) {
							VirtualEthernetCard nic = null;
							VirtualMachineConfigInfo vmConfigInfo = vm.getConfig();
							VirtualDevice[] test = vmConfigInfo.getHardware().getDevice();
							VirtualDeviceConfigSpec nicSpec = new VirtualDeviceConfigSpec();
							nicSpec.setOperation(VirtualDeviceConfigSpecOperation.remove);
							if(!sameVM) {
								bw_deletedMACs.write(vmList.get(i) + "\t");
								sameVM = true;
							}

							for(int k=0; k<test.length; k++) {
								if(test[k].getDeviceInfo().getLabel().contains("Network adapter")) {
									nic = (VirtualEthernetCard)test[k];
									//Write the MAC to the deletedMAC file
									if(nic.getBacking() instanceof VirtualEthernetCardNetworkBackingInfo) {
										VirtualEthernetCardNetworkBackingInfo nicBacking = (VirtualEthernetCardNetworkBackingInfo) nic.getBacking();
										if(nicBacking.getDeviceName().equalsIgnoreCase(addpgList.get(j))) {
											bw_deletedMACs.write(nicBacking.getDeviceName() + "\t");
											bw_deletedMACs.write(nic.macAddress + "\t");
											nicSpec.setDevice(nic);
											break;
										}
									}
								}
							}
							nicSpecArray[j] = nicSpec;

						}
						vmConfigSpec.setDeviceChange(nicSpecArray);

						Task task = vm.reconfigVM_Task(vmConfigSpec);

						PrintMsg.ItemPrint("Deleting " + addpgList.size() + " vNics from  VM : " + vmList.get(i));
						String status = task.waitForTask();
						if (!status.equals("success")) {
							PrintMsg.ErrorPrint("Error Deleting vNics From VM : " + vmList.get(i));
							finalStatus = "FAIL";
							return finalStatus;
						}else {
							//Write the remaining MACs from the VM to the remainingMACs file
							VirtualMachine reconfigVM = (VirtualMachine) new InventoryNavigator(rootFolder).searchManagedEntity("VirtualMachine", vmList.get(i));
							writeVMMACstoFile(rootFolder, vmList.get(i), reconfigVM, dvswitchList, dvpgMap, bw_remainingMACs);
						}
					}
				}
			}
			bw_remainingMACs.close();
			bw_deletedMACs.write("\n");
			bw_deletedMACs.close();

		} catch (Exception e) {
			PrintMsg.ErrorPrint("Error Deleting VNICs: " + e);
			finalStatus = "FAIL";
			return finalStatus;
		}
	

		return finalStatus;
	}



	public static String AddDvnic(ServiceInstance si, Map<String, ArrayList<String>> deletevmMap, ArrayList<String>dvswitchList, Map<String, ArrayList<String>>dvpgMap, ArrayList<String> hostList, ArrayList<String> adddvpgList, Map<String, ArrayList<String>> adddvpgMap, ArrayList<String> datacenterList) throws Exception { 

	 	PrintMsg.HeaderPrint("VMOps Command Execution: AddDvnic");
		String finalStatus = "SUCCESS";
		String fileName = "./RunOutput/VMMAC_ADDDvnic_"+datacenterList.get(0)+".txt";
		BufferedWriter bw = writeFile(fileName);

		try {

			Folder rootFolder = si.getRootFolder();
			PrintMsg.MsgPrint("Adding Following DvNICS : ");
			for (int e=0; e<adddvpgList.size(); e++) {
				PrintMsg.ItemPrint(adddvpgList.get(e));
			}

			for (int a=0; a<hostList.size(); a++) {
				ArrayList<String> vmList = deletevmMap.get(hostList.get(a)); 
				
				for (int i=0; i<vmList.size(); i++) {

					VirtualMachine vm = (VirtualMachine) new InventoryNavigator(rootFolder).searchManagedEntity("VirtualMachine", vmList.get(i));
					if(vm!=null) {
			
						VirtualMachineConfigSpec vmConfigSpec = new VirtualMachineConfigSpec();

						VirtualDeviceConfigSpec[] nicSpecArray = new VirtualDeviceConfigSpec[adddvpgList.size()];
						for (int j=0; j<adddvpgList.size(); j++) {
							ArrayList<String> adddvpgDVSList = adddvpgMap.get(adddvpgList.get(j));
							for(int k=0; k<adddvpgDVSList.size(); k++) {
								DistributedVirtualPortgroup dvpg = (DistributedVirtualPortgroup) new InventoryNavigator(rootFolder).searchManagedEntity("DistributedVirtualPortgroup", adddvpgList.get(j));
								VmwareDistributedVirtualSwitch dvs = (VmwareDistributedVirtualSwitch) new InventoryNavigator(rootFolder).searchManagedEntity("DistributedVirtualSwitch", adddvpgDVSList.get(k)); 
								VirtualDeviceConfigSpec nicSpec = new VirtualDeviceConfigSpec();
								VirtualMachineConfigInfo vmConfigInfo = vm.getConfig();

								nicSpec.setOperation(VirtualDeviceConfigSpecOperation.add);
								VirtualEthernetCard nic = new VirtualPCNet32();
								VirtualEthernetCardDistributedVirtualPortBackingInfo nicBacking = new VirtualEthernetCardDistributedVirtualPortBackingInfo();
								DistributedVirtualSwitchPortConnection portConnection = new DistributedVirtualSwitchPortConnection();
								portConnection.setPortgroupKey(dvpg.getKey());
								portConnection.setSwitchUuid(dvs.getSummary().getUuid());
								nicBacking.setPort(portConnection);
								nic.setAddressType("generated");
								nic.setBacking(nicBacking);
								nicSpec.setDevice(nic);
								nicSpecArray[j] = nicSpec;
							}

						}
						vmConfigSpec.setDeviceChange(nicSpecArray);

						Task task = vm.reconfigVM_Task(vmConfigSpec);
						PrintMsg.ItemPrint("Adding " + adddvpgList.size() + " DvNics to  VM : " + vmList.get(i));
						String status = task.waitForTask();
						if (!status.equals("success")) {
							PrintMsg.ErrorPrint("Error Adding DvNic VM : " + vmList.get(i));
							finalStatus = "FAIL";
							return finalStatus;
						}else {
							VirtualMachine reconfigVM = (VirtualMachine) new InventoryNavigator(rootFolder).searchManagedEntity("VirtualMachine", vmList.get(i));
							VirtualMachineConfigInfo vmConfigInfo = reconfigVM.getConfig();
							VirtualDevice[] vds = vmConfigInfo.getHardware().getDevice();
							bw.write(vmList.get(i)+"\t");
							for(int m=0; m<vds.length; m++) {
								if((vds[m] instanceof VirtualEthernetCard)) {
									VirtualEthernetCard veCard = (VirtualEthernetCard) vds[m]; 
									if(veCard.getBacking() instanceof VirtualEthernetCardDistributedVirtualPortBackingInfo) {
										VirtualEthernetCardDistributedVirtualPortBackingInfo nicBacking = (VirtualEthernetCardDistributedVirtualPortBackingInfo) veCard.getBacking();
										boolean gotDVNIC = false;
										for (int d=0; d<adddvpgList.size(); d++) {
											DistributedVirtualPortgroup dvpg = (DistributedVirtualPortgroup) new InventoryNavigator(rootFolder).searchManagedEntity("DistributedVirtualPortgroup", adddvpgList.get(d));
											if(nicBacking.getPort().getPortgroupKey().equalsIgnoreCase(dvpg.getKey())) {
													bw.write(adddvpgList.get(d) + "\t");
													bw.write(veCard.macAddress + "\t");
													gotDVNIC = true;
													break;
												}


										}
										
										//if DVNIC is not found it should be in the original dvpgList that was already attached to the vm
										if(!gotDVNIC) {
											for (int b=0; b<dvswitchList.size(); b++) {
												ArrayList<String> dvnicList = dvpgMap.get(dvswitchList.get(b));
												for(int c=0; c<dvnicList.size(); c++) {
													DistributedVirtualPortgroup dvpg = (DistributedVirtualPortgroup) new InventoryNavigator(rootFolder).searchManagedEntity("DistributedVirtualPortgroup", dvnicList.get(c));
													if(nicBacking.getPort().getPortgroupKey().equalsIgnoreCase(dvpg.getKey())) {
														bw.write(dvnicList.get(c) + "\t");
														bw.write(veCard.macAddress + "\t");
														gotDVNIC = true;
														break;
													}
												}
												if(gotDVNIC) break;
											}
										}
									} else {
										VirtualEthernetCardNetworkBackingInfo nicBacking = (VirtualEthernetCardNetworkBackingInfo) veCard.getBacking();
										bw.write(nicBacking.getDeviceName() + "\t");
										bw.write(veCard.macAddress + "\t");
									}
								}
							}
							bw.write("\n");
						}
					}
				}
			}
			bw.close();

		} catch (Exception e) {
			PrintMsg.ErrorPrint("Error Adding VNICs: " + e);
			finalStatus = "FAIL";
			return finalStatus;
		}
	

		return finalStatus;
	}



	public static String DeleteDvnic(ServiceInstance si, Map<String, ArrayList<String>> deletevmMap, ArrayList<String>dvswitchList, Map<String, ArrayList<String>>dvpgMap, ArrayList<String> hostList, ArrayList<String> adddvpgList, ArrayList<String> datacenterList) throws Exception { 

	 	PrintMsg.HeaderPrint("VMOps Command Execution: DeleteDvnic");
		String finalStatus = "SUCCESS";
		String fileName_deletedMACs = "./RunOutput/VMMAC_DELDvnicDeletedMACs_"+datacenterList.get(0)+".txt";
		String fileName_remainingMACs = "./RunOutput/VMMAC_DELDvnicRemainingMACs_"+datacenterList.get(0)+".txt";

		BufferedWriter bw_deletedMACs = writeFile(fileName_deletedMACs);
		BufferedWriter bw_remainingMACs = writeFile(fileName_remainingMACs);

		try {

			Folder rootFolder = si.getRootFolder();
			
			PrintMsg.MsgPrint("Deleting Following DvNICS : ");
			for (int e=0; e<adddvpgList.size(); e++) {
				PrintMsg.ItemPrint(adddvpgList.get(e));
			}
			boolean newFile = true;
			for (int a=0; a<hostList.size(); a++) {
				
				ArrayList<String> vmList = deletevmMap.get(hostList.get(a)); 
				
				for (int i=0; i<vmList.size(); i++) {
		
					VirtualMachine vm = (VirtualMachine) new InventoryNavigator(rootFolder).searchManagedEntity("VirtualMachine", vmList.get(i));
					if (vm!=null) {
						if(!newFile) {
							bw_deletedMACs.write("\n");
						}
						newFile = false;

						VirtualMachineConfigSpec vmConfigSpec = new VirtualMachineConfigSpec();
						VirtualDeviceConfigSpec[] nicSpecArray = new VirtualDeviceConfigSpec[adddvpgList.size()];

						boolean sameVM = false;
						for (int j=0; j<adddvpgList.size(); j++) {
							DistributedVirtualPortgroup dvpg = (DistributedVirtualPortgroup) new InventoryNavigator(rootFolder).searchManagedEntity("DistributedVirtualPortgroup", adddvpgList.get(j));
							VirtualEthernetCard nic = null;
							VirtualMachineConfigInfo vmConfigInfo = vm.getConfig();
							VirtualDevice[] test = vmConfigInfo.getHardware().getDevice();
							VirtualDeviceConfigSpec nicSpec = new VirtualDeviceConfigSpec();
							nicSpec.setOperation(VirtualDeviceConfigSpecOperation.remove);

							if(!sameVM) {
								bw_deletedMACs.write(vmList.get(i) + "\t");
								sameVM = true;
							}
		
							for(int k=0; k<test.length; k++) {
								if(test[k].getDeviceInfo().getLabel().contains("Network adapter")) {
									nic = (VirtualEthernetCard)test[k];
									//Write the MAC to the deletedMAC file
									if(nic.getBacking() instanceof VirtualEthernetCardDistributedVirtualPortBackingInfo) {
										VirtualEthernetCardDistributedVirtualPortBackingInfo nicBacking = (VirtualEthernetCardDistributedVirtualPortBackingInfo) nic.getBacking();
										if(nicBacking.getPort().getPortgroupKey().equalsIgnoreCase(dvpg.getKey())) {
											bw_deletedMACs.write(adddvpgList.get(j) + "\t");
											bw_deletedMACs.write(nic.macAddress + "\t");
											nicSpec.setDevice(nic);
											break;
										}
									}
								}
							}
							nicSpecArray[j] = nicSpec;

						}
						vmConfigSpec.setDeviceChange(nicSpecArray);

						Task task = vm.reconfigVM_Task(vmConfigSpec);

						PrintMsg.ItemPrint("Deleting " + adddvpgList.size() + " DvNics from  VM : " + vmList.get(i));
						String status = task.waitForTask();
						if (!status.equals("success")) {
							PrintMsg.ErrorPrint("Error Deleting DvNics From VM : " + vmList.get(i));
							finalStatus = "FAIL";
							return finalStatus;
						}else {
							//Write the remaining MACs from the VM to the remainingMACs file
							VirtualMachine reconfigVM = (VirtualMachine) new InventoryNavigator(rootFolder).searchManagedEntity("VirtualMachine", vmList.get(i));
							VirtualMachineConfigInfo vmConfigInfo = reconfigVM.getConfig();
							VirtualDevice[] vds = vmConfigInfo.getHardware().getDevice();
							bw_remainingMACs.write(vmList.get(i)+"\t");
							for(int m=0; m<vds.length; m++) {
								if((vds[m] instanceof VirtualEthernetCard)) {
									VirtualEthernetCard veCard = (VirtualEthernetCard) vds[m]; 
									if(veCard.getBacking() instanceof VirtualEthernetCardDistributedVirtualPortBackingInfo) {
										VirtualEthernetCardDistributedVirtualPortBackingInfo nicBacking = (VirtualEthernetCardDistributedVirtualPortBackingInfo) veCard.getBacking();
										boolean gotDVNIC = false;
										for (int b=0; b<dvswitchList.size(); b++) {
											ArrayList<String> dvnicList = dvpgMap.get(dvswitchList.get(b));
											for(int c=0; c<dvnicList.size(); c++) {
												DistributedVirtualPortgroup dvpg = (DistributedVirtualPortgroup) new InventoryNavigator(rootFolder).searchManagedEntity("DistributedVirtualPortgroup", dvnicList.get(c));
												if(nicBacking.getPort().getPortgroupKey().equalsIgnoreCase(dvpg.getKey())) {
													bw_remainingMACs.write(dvnicList.get(c) + "\t");
													bw_remainingMACs.write(veCard.macAddress + "\t");
													break;
												}
											}
											if(gotDVNIC) break; 
										}

									} else {
										VirtualEthernetCardNetworkBackingInfo nicBacking = (VirtualEthernetCardNetworkBackingInfo) veCard.getBacking();
										bw_remainingMACs.write(nicBacking.getDeviceName() + "\t");
										bw_remainingMACs.write(veCard.macAddress + "\t");


									}
								}
							}
							bw_remainingMACs.write("\n");

						}
					}
				}
			}
			bw_remainingMACs.close();
			bw_deletedMACs.write("\n");
			bw_deletedMACs.close();

		} catch (Exception e) {
			PrintMsg.ErrorPrint("Error Deleting VNICs: " + e);
			finalStatus = "FAIL";
			return finalStatus;
		}
	

		return finalStatus;
	}


	private static void writeVMMACstoFile(Folder rootFolder, String vmName, VirtualMachine vm, ArrayList<String> dvswitchList, Map<String, ArrayList<String>> dvpgMap, BufferedWriter bw)  {

		try {

			VirtualMachineConfigInfo vmConfigInfo = vm.getConfig();
			VirtualDevice[] vds = vmConfigInfo.getHardware().getDevice();
			bw.write(vmName+"\t");
			for(int m=0; m<vds.length; m++) {
				if((vds[m] instanceof VirtualEthernetCard)) {
					VirtualEthernetCard veCard = (VirtualEthernetCard) vds[m]; 
					boolean gotDVNIC = false;
					if(veCard.getBacking() instanceof VirtualEthernetCardDistributedVirtualPortBackingInfo) {
						VirtualEthernetCardDistributedVirtualPortBackingInfo nicBacking = (VirtualEthernetCardDistributedVirtualPortBackingInfo) veCard.getBacking();
						for (int b=0; b<dvswitchList.size(); b++) {
							ArrayList<String> dvnicList = dvpgMap.get(dvswitchList.get(b));
							for(int c=0; c<dvnicList.size(); c++) {
								DistributedVirtualPortgroup dvpg = (DistributedVirtualPortgroup) new InventoryNavigator(rootFolder).searchManagedEntity("DistributedVirtualPortgroup", dvnicList.get(c));
								if(nicBacking.getPort().getPortgroupKey().equalsIgnoreCase(dvpg.getKey())) {
									bw.write(dvnicList.get(c) + "\t");
									bw.write(veCard.macAddress + "\t");
									gotDVNIC = true;
									break;
								}
							}
							if(gotDVNIC) break;
						}
					} else { // this vnic belongs to a std pg
						VirtualEthernetCardNetworkBackingInfo nicBacking = (VirtualEthernetCardNetworkBackingInfo) veCard.getBacking();
						bw.write(nicBacking.getDeviceName() + "\t");
						bw.write(veCard.macAddress + "\t");
					}
				}
			}
			bw.write("\n");

		} catch (Exception e) {
			PrintMsg.ErrorPrint("Error Writing VM MAC Addresses: " + e);
		}

	}







	public static String DeleteHost(ServiceInstance si, ArrayList<String> hostList) throws Exception {
		
	 	PrintMsg.HeaderPrint("VMOps Command Execution: DeleteHost");
		String finalStatus = "SUCCESS";

		try {
			Folder rootFolder = si.getRootFolder();
			for (int i=0; i<hostList.size(); i++) {

				HostSystem host = (HostSystem) new InventoryNavigator(rootFolder).searchManagedEntity("HostSystem", hostList.get(i));
	
				Task task = host.getParent().destroy_Task();
				PrintMsg.ItemPrint("Delete Host: " +  hostList.get(i));

				String status = task.waitForTask();
				if (!status.equals("success")) {
					PrintMsg.ErrorPrint("Error Deleting Host: " + hostList.get(i));
					finalStatus = "FAIL";
					return finalStatus;
				}else {
				}

			}
		} catch (Exception e) {
			PrintMsg.ErrorPrint("Error Deleting Hosts: " + e);
			finalStatus = "FAIL";
			return finalStatus;
		}
		return finalStatus;
	
	}		

	public static String AddHost(ServiceInstance si, ArrayList<String> datacenterList,  ArrayList<String> hostList) throws Exception {
		
	 	PrintMsg.HeaderPrint("VMOps Command Execution: AddHost");
		String finalStatus = "SUCCESS";

		try {
			Folder rootFolder = si.getRootFolder();
			Datacenter dc = (Datacenter) new InventoryNavigator(rootFolder).searchManagedEntity("Datacenter", datacenterList.get(0));
			Folder hostFolder = dc.getHostFolder();
	
			for (int i=0; i<hostList.size(); i++) {
				HostConnectSpec hSpec = new HostConnectSpec();
				hSpec.hostName = hostList.get(i);
				hSpec.force = true;
				hSpec.port = 443;
				hSpec.userName = "root";
				hSpec.password = "password";
				ComputeResourceConfigSpec addConnected = new ComputeResourceConfigSpec();
				Task task = hostFolder.addStandaloneHost_Task(hSpec, addConnected, true, null);

				PrintMsg.ItemPrint("Adding Host: " +  hostList.get(i));

				String status = task.waitForTask();
				if (!status.equals("success")) {
					if (task.getTaskInfo().error.fault instanceof SSLVerifyFault) {
						SSLVerifyFault sslFault = (SSLVerifyFault) task.getTaskInfo().error.fault;
						hSpec.sslThumbprint = sslFault.getThumbprint();
						Task task2 = hostFolder.addStandaloneHost_Task(hSpec, addConnected, true, null);
						status = task2.waitForTask();
					}
					if(!status.equals("success")) {
						PrintMsg.ErrorPrint("Error Adding Host: " + hostList.get(i));
						finalStatus = "FAIL";
						return finalStatus;
					}
				}else {
				}

			} } catch (Exception e) {
			PrintMsg.ErrorPrint("Error Adding Hosts: " + e);
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

}



