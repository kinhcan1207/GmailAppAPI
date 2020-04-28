/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gmailApi;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFileChooser;

/**
 *
 * @author Admin
 */
public class XuLyFile {

    public static boolean deleteDirectory(File dir) {
	if (dir.isDirectory()) {
	    File[] children = dir.listFiles();
	    for (int i = 0; i < children.length; i++) {
		boolean success = deleteDirectory(children[i]);
		if (!success) {
		    return false;
		}
	    }
	} // either file or an empty directory 
	System.out.println("removing file or directory : " + dir.getName());
	return dir.delete();
    }

    public static List<String> listAllFileInDirectory(String path) {
	List<String> listFile = new ArrayList<>();
	File folder = new File(path);
	File[] listOfFiles = folder.listFiles();

	for (File listOfFile : listOfFiles) {
	    listFile.add(listOfFile.getName());
	}
	return listFile;
    }

    /**
     * Mở 1 hộp thoại cho chọn đường dẫn
     * @return
     */
    public static String showOpenDirDialog() {
	JFileChooser fileChooser = new JFileChooser();
	fileChooser.setDialogTitle("Chọn thư mục");
	String dirPath = "";
	fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	int userSelection = fileChooser.showOpenDialog(null);
	if (userSelection == JFileChooser.APPROVE_OPTION) {
	    dirPath = fileChooser.getSelectedFile().toString();
	}
	return (dirPath + "/");
    }
}
