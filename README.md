# 422-data-obfuscation
fall 2023, iste 422 app development practices, ex08 regarding data obfuscation using java and gradlew

# Obfuscation Details
#### For `String` data types ⋆·˚ ༘ * Replaced all lowercase letters with an “x” 
- `Owner` SSN, name, address, address2, city, state, zip
- `Account` name, `RegisterEntry` entryName

#### For `Int` data types ⋆·˚ ༘ * Replaced numbers 0-9 with 1 
- `Owner` Id
- `Account` Id

#### For `Date` data types ⋆·˚ ༘ * Added 8 days to original date 
- `Owner` dob
- `RegisterEntry` date

#### For `Double` data types ⋆·˚ ༘ * Replaced numbers 0-9 with 4 
- `Account` balance
- `RegisterEntry` amount

#### For `Long` data types ⋆·˚ ༘ * Replaced numbers 0-9 with 5 
- `Account` ownerId
- `RegisterEntry` id, accountId


### Issues Encountered
- Unsurprisingly, Replit was not very cooperative and happy about using java and javac. I had to use ./gradlew commands instead of gradle commands to overcome this issue.
- Obfuscating Date objects required some more work. I had to import the DateFormat, Date, SimpleDateFormat and Calendar libraries to successfully obfuscate an owner’s date of birth data properly.
- I wasn’t able to access the Account objects like the Owner objects (o.id(), o.name() etc.) because the classes were set up differently. I had to use the get methods inside Account to get the variables I needed to obfuscate.
- When creating the newAccounts array, upon adding a new Account given the obfuscated variables, I was getting an error: Account is abstract; cannot be instantiated. I had to override the monthEnd() method when creating a new Account class to solve this problem.
