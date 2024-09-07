import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;
//This is a single class program which demonstrates basic manipulation of directories and files
//it makes use of NIO2 functionalities and recursion

//the output from this program is 1 folder with 2 subfolders, and each subfolder has a file inside
//this file will contain path data from its current directory plus all sub/children
public class Main
{
	private static Map<Path, List<Path>> directories;
	public static void main(String[] args)
	{
		directories = new LinkedHashMap<>();
		Path base = Path.of(".\\public");
		try
		{
			//call our main writing function, which uses recursion
			recurseWrite(base);
		}
		catch(IOException ioe)
		{
			ioe.printStackTrace();
		}

		//this just loops through our directories and prints it
		//the true output is contained in the various index.txt files
		for(var k : directories.keySet())
		{
			StringBuilder s = new StringBuilder();
			List<Path> ref = directories.get(k);
			ref.forEach(x -> s.append("\"" + x + "\" - "));
			String trimmed;
			if(s.length() > 3)
			{
				trimmed = s.substring(0, s.length() - 3).toString();
			}
			else
			{
				trimmed = s.toString();
			}
			System.out.println(k + ": " + trimmed);
		}
	}

	//main function that will recursively call itself to gather children
	//and write the information to a file called index.txt
	//it takes a Path for an argument, and will recursively call itself if it finds children in a directory
	private static void recurseWrite(Path target) throws IOException
	{
		//if the target this call is a folder/directory
		if(Files.isDirectory(target))
		{
			//generate a stream of all children in the directory
			try(Stream<Path> children = Files.list(target))
			{
				List<Path> childList = children.toList(); //make the stream into a list
				//for each child object (file or folder) on the list
				childList.forEach( p ->
				{
					//accumulate children into a linkedhashmap
					try
					{
						//first get existing list of children, if any
						//if there are no children, make a new arraylist and add our new paths
						List<Path> workingChildList = directories.get(target);
						if(workingChildList == null)
						{
							workingChildList = new ArrayList<Path>();
						}
						workingChildList.add(p);
						directories.put(target, workingChildList);
						//resolve() concatenates paths, then recursively call with the new path
						Main.recurseWrite(target.resolve(p.getFileName()));
					}
					catch(IOException ioe)
					{
						ioe.printStackTrace();
					}
				});
				//if there are no more children
				//in other words, the current path has no subfolders or subobjects
				if(childList.isEmpty())
				{
					var newChildList = directories.get(target);
					if(newChildList == null)
					{
						newChildList = new ArrayList<Path>();
					}
					directories.put(target, newChildList);
				}
			}
		}
		Path indexFilePath = target;
		if(Files.isDirectory(indexFilePath))
		{
			indexFilePath = target.resolve("index.txt"); //we're adding index.txt to directory paths
		}
		else if(target.endsWith("index.txt")) //if we're looking at Index.txt already
		{
			indexFilePath = target; //no need to add "index.txt" to index.txt
		}
		else //if the target is not a directory nor is it index.txt, ignore it
		{
			return;
		}

		//we don't need to try/catch because the method already throws IOException
		if(!Files.exists(indexFilePath)) //if index.txt doesn't exist
		{
			//create index.txt, update our directories map
			Files.createFile(indexFilePath);
			List<Path> dirPaths = directories.get(target);
			dirPaths.add(indexFilePath);
			directories.put(target, dirPaths);
		}
		else //index.txt already exists
		{
			System.out.println("File already exists - " + indexFilePath + ". Re-creating it.");
			Files.delete(indexFilePath);
			Files.createFile(indexFilePath);
			List<Path> dirPaths = directories.get(target);
			if(dirPaths == null)
			{
				dirPaths = new ArrayList<Path>();
			}
			directories.put(target, dirPaths);
		}

		//using BufferedWriter as NIO2
		//we also should use try/catch here because it will auto-close the resource
		try(BufferedWriter bw = Files.newBufferedWriter(indexFilePath))
		{
			bw.write("looking at path: " + target.toString());
			bw.newLine();

			//set up keysets and keylists for looping
			Set<Path> keySet = directories.keySet();
			List<Path> keyList = new ArrayList<Path>(keySet);
			int listIndex = keyList.indexOf(target);

			//loop through our directory lists for writing
			for(int i = listIndex; i < keyList.size(); i++)
			{
				List<Path> writePath = directories.get(keyList.get(i));
				StringBuilder sb = new StringBuilder();
				for(var paths : writePath)
				{
					sb.append(paths + "; ");
				}
				if(sb.length() > 2)
				{
					sb.substring(0, sb.length() - 2);
				}
				bw.write(sb.toString());
				bw.newLine();
			}
		}
		catch(IOException ioe)
		{
			ioe.printStackTrace();
		}
	}
}
