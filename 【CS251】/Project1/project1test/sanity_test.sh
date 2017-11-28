javac Project1.java
java Project1 < data/test$1.data.txt > data/output$1.data.txt

if cmp -s data/output$1.data.txt data/expected_output$1_data.txt
then
   echo "Sanity Test Passed!"
else
   echo "Output Differs"
fi
