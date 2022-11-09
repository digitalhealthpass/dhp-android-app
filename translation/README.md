# Translation Python script which can use local file or connect to Google sheet, depending on your needs, then will convert it to Strings files (works for ios as well)
  - Download Python 3 https://www.python.org/downloads/mac-osx/
  - Download needed libraries for the script using
  `pip3 install --upgrade numpy`
  `pip3 install --upgrade pandas`
  `pip3 install --upgrade openpyxl`
  and any other error that says "no module found in your logs"
  
  - download file https://ibm.ent.box.com/file/794148140889 from Box (Translation String.xlsx) and put inside the `translation` package
  - open your terminal, navigate to the Python file, then use `python3 translate.py` (the command `python3` can be `python` if you are using an older version)
  - By default the script will run the file with multi language columns. However if you want to run it for an individual file, then look for `lang_class = LangFile("de")` in the script
  and adjust the language code that you are trying to parse, this will create translation file only for this language code you used.

# Script logic:
 - The script by default is using one File which has multiple string columns where each column is the Language, ex. `en, de, fr`, adding multiple sheets in the file will cause the
  script to combine all sheets with name `multi` and add all `Key` to use it, so you can potentially divide the languages into multiple sheets but now you have to maintain the `Key`.
 - You can let the script read a single file which has only one language, by going to the script and activate `lang_class = LangFile("de")` and disable the `MultiLang` class.
 - Essentially you can do that to multiple files and you would have to provide each Language Code for the `LangFile` class
 - You can also integrate with Google docs and use it online directly, by using the `FromGoogle` class.