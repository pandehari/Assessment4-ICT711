# \# Employee Performance Management System (EPMS)

# 

# \## Individual Assessment 3 Project

# 

# This project is my individual work for the same Assessment 3 topic that our team worked on before. The project is an \*\*Employee Performance Management System (EPMS)\*\* developed using Java.

# 

# The main purpose of this system is to manage employee performance information. It allows the user to add, edit, delete, search, query and sort employee records. The system also supports loading and saving employee information using CSV files.

# 

# The project has both a \*\*Graphical User Interface (GUI)\*\* using Java Swing and a \*\*Text-Based Interface (TBI)\*\* using a console menu.

# 

# \## Project File Structure

# 

# The project files have been created in the following way:

# 

# \~\~\~text

# src/epms/

# &#x20; App.java

# &#x20; model/

# &#x20;   Employee.java

# &#x20;   EmployeeField.java

# &#x20; algorithms/

# &#x20;   SortAlgorithm.java

# &#x20;   Sorter.java

# &#x20;   SortMetrics.java

# &#x20;   SortResult.java

# &#x20;   Searcher.java

# &#x20;   SearchResult.java

# &#x20; data/

# &#x20;   EmployeeRepository.java

# &#x20;   FileStorage.java

# &#x20;   DuplicateEmployeeException.java

# &#x20;   EmployeeNotFoundException.java

# &#x20; ui/

# &#x20;   text/

# &#x20;     TextInterface.java

# &#x20;   gui/

# &#x20;     MainWindow.java

# &#x20;     EmployeeTableModel.java

# &#x20;     EmployeeFormDialog.java

# &#x20;     CellRenderers.java

# 

# test/epms/

# &#x20; SorterTest.java

# &#x20; SearcherTest.java

# &#x20; EmployeeRepositoryTest.java

# &#x20; FileStorageTest.java

# &#x20; AlgorithmPerformanceTest.java

# 

# data/

# &#x20; employees.csv

# 

# run.sh

# build.sh

# test.sh

# bench.sh

# \~\~\~

# 

# \## How the Project Has Been Done

# 

# The project was developed using \*\*Java\*\* and is designed for \*\*JDK 17 or later\*\*. The employee details are managed through the `EmployeeRepository`, which is shared between both the GUI and text-based interfaces.

# 

# Different sorting algorithms have been manually implemented, including \*\*Bubble Sort, Insertion Sort, Selection Sort, Quick Sort and Merge Sort\*\*. Linear Search and Binary Search are also implemented for searching employee records.

# 

# Employee data can be saved to and loaded from a CSV file. Validation is also included to prevent incorrect employee information and duplicate employee IDs.

# 

# Testing has been completed using \*\*JUnit 5\*\*. The tests cover sorting, searching, employee management, CSV file operations and algorithm performance.

# 

# \## How to Run the Project

# 

# The program can be started from the main `run.sh` file:

# 

# \~\~\~bash

# ./run.sh

# \~\~\~

# 

# After running this file, the program asks the user to select either the \*\*Graphical User Interface (GUI)\*\* or \*\*Text-Based Interface (TBI)\*\*.

# 

# The GUI can also be opened directly using:

# 

# \~\~\~bash

# ./run.sh --gui

# \~\~\~

# 

# The text-based version can be opened directly using:

# 

# \~\~\~bash

# ./run.sh --text

# \~\~\~

# 

# The project can be tested using:

# 

# \~\~\~bash

# ./test.sh

# \~\~\~

# 

# Overall, this project demonstrates employee data management, file handling, searching and sorting algorithms, GUI development and software testing using Java.

