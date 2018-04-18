JAVA_VER=$(java -version 2>&1 | sed -n ';s/.* version "\(.*\)\.\(.*\)\..*"/\1\2/p;')
if [ "$JAVA_VER" -ge 18 ]; then
	echo "Found a JRE that works !"
	INSTALL_DIR="$(cd "$(dirname "$0")" && pwd)"
	declare -a arr=(
		"alias putwb-ui='java -jar $INSTALL_DIR/putwb-1.51-complete.jar'"
		"alias putwb='java -cp $INSTALL_DIR/putwb-1.51-complete.jar in.ac.iitk.cse.putwb.experiment.PUTExperiment'"
		"alias putwb-rec='java -cp $INSTALL_DIR/putwb-1.51-complete.jarr in.ac.iitk.cse.putwb.experiment.RecoveryManager'"
		"alias putwb-ver='java -cp $INSTALL_DIR/putwb-1.51-complete.jarr in.ac.iitk.cse.putwb.experiment.Verifier'"
	)
	>~/.putwb_settings
	for i in "${arr[@]}"
	do
		echo "$i" >> ~/.putwb_settings
	done
	
	echo "This script creates some aliases to make your life easier. I'll use the $HOME/.bashrc file for the same."
	echo "Shall I ho ahead? Type 'Y' to continue:"
	read choice
	
	if [ "$choice" == "Y" ] || [ "$choice" == "y" ]
	then
		echo '# PUTWorkbench Settings' >> ~/.bashrc
		echo '. ~/.putwb_settings' >> ~/.bashrc
		echo 'Done !!'
	else
		"I've created a file that contains all aliases '$HOME/.putwb_settings'. You can source it before invoking the tool."
	fi
else
	echo "Your JRE is old, the tool may or may not work. We suggest you get a new one, 1.8 or better would work !"
fi
