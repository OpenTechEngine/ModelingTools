mkdir e:\temp\md3view
cd ...
jar cfm e:\temp\md3view\md3view.jar md3\md3view\manifest md3\md3model\*.class md3\md3view\*.class md3\md3view\visitor\*.class md3\md3view\glmodel\*.class md3\util\*.class md3\md3view\*.gif md3\md3view\*.jpg md3\md3view\gpl.txt widgets\awt\*.class widgets\awt\event\*.class cio\*.class
mkdir docs
javadoc -d docs -author -protected md3.md3model md3.md3view md3.md3view.glmodel md3.md3view.visitor md3.util widgets.awt widgets.awt.event cio
zip -r e:\temp\md3view\sources.zip md3\* widgets\* cio\* docs\* -x *.class
rmdir /s docs
copy md3\md3view\README.html e:\temp\md3view
REM copy md3\md3view\MD3ViewApplet.html e:\temp\md3view
copy md3\md3view\md3view.bat e:\temp\md3view
copy md3\md3view\gpl.txt e:\temp\md3view