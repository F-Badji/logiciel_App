# IntelliJ IDEA Setup for JavaFX Application

## Problem
When clicking "Run" in IntelliJ IDEA, you get the error:
```
Error: JavaFX runtime components are missing, and are required to run this application
```

## Solution
I've created two run configurations for you:

### Option 1: Maven Run Configuration (RECOMMENDED)
1. In IntelliJ IDEA, go to the top menu bar
2. Click on the dropdown next to the Run button (it might show "MobileUnlockApp" or "Application")
3. Select **"Run JavaFX App (Maven)"**
4. Click the green Run button

This uses Maven's `javafx:run` goal which automatically handles all JavaFX module paths.

### Option 2: Application Run Configuration (with VM options)
1. In the dropdown, select **"MobileUnlockApp"** 
2. Click the green Run button

This is configured with the proper JavaFX VM parameters and module paths.

## How to Access Run Configurations
- Go to **Run** → **Edit Configurations...**
- You should see both configurations listed:
  - "Run JavaFX App (Maven)" - Maven type
  - "MobileUnlockApp" - Application type

## If configurations don't appear:
1. Close IntelliJ IDEA
2. Reopen the project
3. The configurations should now appear in the run dropdown

## Alternative: Maven from IntelliJ Terminal
You can also run from IntelliJ's built-in terminal:
```bash
mvn clean javafx:run
```

## Login Credentials
- Username: `Admin`
- Password: `Serignetouba2020`

## Troubleshooting
If you still have issues:
1. Ensure XAMPP MySQL is running
2. Try refreshing Maven project (right-click on pom.xml → Maven → Reload project)
3. Use the Maven configuration rather than the Application configuration
