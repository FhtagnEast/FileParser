package com.gmail.fhtagneast;

import java.io.File;
import java.util.List;

public interface DirectoryParser {
     File[] getFiles (String directory, String extension);
     List getMatches (List filteredDirectories);

}
