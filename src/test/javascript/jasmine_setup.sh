print_status_message() {
  printf "\n[STATUS]\n\t$1\n\n"
}

printf "\n\n\n\n\t------------------------\n"
printf "\t|JASMINE TESTING SCRIPT|\n"
printf "\t------------------------\n\n\n\n"

printf "\n[INFO]\n"
printf "\tThis script is used to automate the Jasmine testing.\n\n"
printf "\tIt will automatically:\n"
printf "\t- Create the folder structure.\n"
printf "\t- Copy all the files that need to be tested in the src subfolder.\n"
printf "\t- Copy all the files containing tests in the spec subfolder.\n\n"

printf "\n[CAUTION]\n"
printf "\tThe script uses the default Jasmine settings and doesn't provide support for includeing helpers.\n"
printf "\tThey are beyond our scope right now and we consider including them afterwards.\n\n"

printf "\n[INSTRUCTIONS]\n"
printf "\t1. Copy the script to the folder where you want to build the Jasmine project.\n"
printf "\t2. Copy all the files that need to be tested(make sure they have the extension .mjs).\n"
printf "\t3. Copy all the files containing tests (make sure they follow the convention *[sS]pec.js).\n\n"

printf "\n[DELETE]\n"
printf "\tThe script will delete the Jasmine project afterwards.\n"

cd src/test/javascript

print_status_message "Installing Jasmine."
npm install -g jasmine

print_status_message "Creating folder hierarchy."
mkdir jasmine
cd jasmine
jasmine init
mkdir src

print_status_message "Adding the files that need be tested."
find .. -name "*.mjs" -exec cp {} src/ \;

print_status_message "Adding the files containing the tests."
find .. -name "*[sS]pec.js" -exec cp {} spec/ \;

print_status_message "Running the tests."
jasmine

cd ..

print_status_message "Deleting the duplicated files."
rm -rf jasmine

print_status_message "The script was completed."
printf "\t-----------\n"
printf "\t|GOOD LUCK|\n"
printf "\t-----------\n\n"
