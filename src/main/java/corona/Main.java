package corona;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class Main {

	private static final String INPUT_FOLDER = "./src/main/java/corona/INPUT";
	private static final String OUTPUT_FOLDER = "./src/main/java/corona/OUTPUT";

	public static void main(String[] args) throws IOException, ParseException {
		
		List<String> inputs = Files.readAllLines(new File(INPUT_FOLDER + "/INPUT.txt").toPath());
		List<PersonPath> personList = new ArrayList<PersonPath>();
		
		//create person list
		for (String input : inputs) {
			personList.add(new PersonPath(input));
		}
		
		//input cmd
		Scanner sc = new Scanner(System.in);
		
		String cmdInput = sc.nextLine();
		
		//get first target
		String[] cmdInputs = cmdInput.split(" ");
		String targetId = cmdInputs[0];
		
		//get filtering target path
		List<PersonPath> startTargetList = personList.stream().filter(t -> targetId.equals(t.getId())).collect(Collectors.toList());
		
		//write target path file
		BufferedWriter bw = new BufferedWriter(new FileWriter(OUTPUT_FOLDER +"/" + targetId + ".txt"));
		
		for (PersonPath person : startTargetList) {
			bw.write(person.toString());
			bw.newLine();
		}
		
		bw.close();
		
		
		//p2
		String targetTime = cmdInputs[1];
		PersonPath firstPath = null;
		List<PersonPath> resList = new ArrayList<PersonPath>();

		//get firstPath 
		for (PersonPath person : startTargetList) {
			if(checkTime(targetTime, person)) {
				firstPath = person;
				continue;
			}
		}
		
		String nextId = firstPath.getId();
		
		List<PersonPath> targetPersonPathList = personList.stream().filter(t -> nextId.equals(t.getId())).collect(Collectors.toList());
		int idx = targetPersonPathList.indexOf(firstPath);
//		idx += 1;
		
		System.out.println("$$$ " + firstPath);
		
		//check all person list after target person firstPath 
		for (PersonPath person : personList) {
			
			for (int i = idx; i < targetPersonPathList.size(); i++) {
				PersonPath nextTargetPath = targetPersonPathList.get(i);
//				System.out.println(">>>>>> " + nextTarget + " : " + person);
				if( !nextId.equals(person.getId()) && checkTarget(nextTargetPath, person)) {
					System.out.println(">>>>>> " + nextTargetPath + " : " + person);
					resList.add(person);
					break;
				}
			}
		}
		
		
		bw = new BufferedWriter(new FileWriter(OUTPUT_FOLDER +"/CHECK.txt"));
		
		for (PersonPath person : resList) {
			bw.write(person.getId());
			bw.newLine();
		}
		
		bw.close();
		
		//p3
		Map<String, List<PersonPath>> resMap = new TreeMap();
		List<PersonPath> childList = null;
		List<PersonPath> rList = new ArrayList<PersonPath>();;
		Stack<PersonPath> stack = new Stack<PersonPath>();
		Set<String> ids = new TreeSet<String>();
		
		//push first person first path
		stack.add(firstPath);
		
		//check first path 
		while (!stack.isEmpty()) {
			
			PersonPath nextPerson = stack.pop();
			childList = new ArrayList<PersonPath>();
			
			//check all person path
			for (PersonPath person : personList) {
				//ignore condition for parent duplication
				if(ids.contains(person.getId())) {
					continue;
				}
				
				List<PersonPath> nextPersonPathList = personList.stream().filter(t -> nextPerson.getId().equals(t.getId())).collect(Collectors.toList());
				int nextIdx = nextPersonPathList.indexOf(nextPerson);
//				nextIdx += 1;
				
				for (int i = nextIdx; i < nextPersonPathList.size(); i++) {
					PersonPath nextPersonPath = nextPersonPathList.get(i);
//					System.out.println(">>>>>> " + nextTarget + " : " + person);
					if( !nextId.equals(person.getId()) && checkTarget(nextPersonPath, person)) {
//						System.out.println(">>>>>> " + nextPersonPath + " : " + person);
						childList.add(person);
						stack.add(person);
						ids.add(person.getId());
						break;
					}
				}
			}
			resMap.put(nextPerson.getId(), childList);
			nextPerson.setChild(childList);
			rList.add(nextPerson);
		}
		
		
		System.out.println(resMap);
		
		//calculate weight
		rList.get(0).calChildVal();
		rList.sort((t1,t2) -> t2.getWeight() - t1.getWeight());
		
		//write weight
		bw = new BufferedWriter(new FileWriter(OUTPUT_FOLDER +"/CHECK_WEIGHT.txt"));
		
		for (PersonPath person : rList) {
			bw.write(person.getWeight() + " : " + person.getId());
			bw.newLine();
		}
		
		bw.close();
	}
	
	public static boolean checkTime(String targetTime, PersonPath origin) throws ParseException {
		boolean ret = false;
		
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
		Date targetDate = sdf.parse(targetTime);
		
		Date oriStartDate = sdf.parse(origin.getStartTime());
		Date oriEndtDate = sdf.parse(origin.getEndTime());
		
		if(oriStartDate.compareTo(targetDate) <= 0 && oriEndtDate.compareTo(targetDate) >= 0) {
			ret = true;
		}
		
		return ret;
	}
	
	public static boolean checkTarget(PersonPath origin, PersonPath target) throws ParseException {
		boolean ret = false;
		
		if(!origin.getPlace().equals(target.getPlace())) {
			return ret;
		}
		
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
		
		Date oriStartDate = sdf.parse(origin.getStartTime());
		Date oriEndtDate = sdf.parse(origin.getEndTime());
		
		Date targetStartDate = sdf.parse(target.getStartTime());
		Date targetEndtDate = sdf.parse(target.getEndTime());
		
		
		if(targetEndtDate.compareTo(oriStartDate) > 0 && targetEndtDate.compareTo(oriEndtDate) <= 0 ) {
			ret = true;
			System.out.println("case1");
		} else if(targetStartDate.compareTo(oriStartDate) >= 0 && targetStartDate.compareTo(oriEndtDate) < 0) {
			ret = true;
			System.out.println("case2");
		} else  if(oriStartDate.compareTo(targetStartDate) <= 0 && oriEndtDate.compareTo(targetEndtDate) >= 0) {
			ret = true;
			System.out.println("case3");
		} else  if(targetStartDate.compareTo(oriStartDate) <= 0 && targetEndtDate.compareTo(oriEndtDate) >= 0) {
			ret = true;
			System.out.println("case4");
		}
		return ret;
	}
}
class PersonPath {
	private String id;
	private String startTime;
	private String endTime;
	private String place;
	private List<PersonPath> child;
	private int weight;
	
	public PersonPath(String raw) {
		String[] data = raw.split(" ");
		id = data[0];
		startTime = data[1];
		endTime = data[2];
		place = data[3];
	}

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public String getPlace() {
		return place;
	}

	public void setPlace(String place) {
		this.place = place;
	}

	public List<PersonPath> getChild() {
		return child;
	}
	public void setChild(List<PersonPath> child) {
		this.child = child;
	}
	
	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	public int calChildVal() {
		int ret = 0;
		
		for (PersonPath person : child) {
			ret += person.calChildVal();
		}
		
		weight = ret + child.size();
		return weight;
	}

	@Override
	public String toString() {
		return String.format("%s %s %s %s", id, startTime, endTime, place);
	}
}

