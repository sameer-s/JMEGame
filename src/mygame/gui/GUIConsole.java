package mygame.gui;

import com.jme3.input.InputManager;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import javax.swing.JOptionPane;

/**
 * A 'console' to allow the user to enter text-based commands.
 * @author Sameer Suri
 */
public class GUIConsole implements ActionListener
{
    private CommandRunner commandRunner; // The command runner (what happens once a command has been inputted)

    public String prompt = "Enter a command:"; // The default prompt

    /**
     * Initializes the console & keypresses
     * @param man The input manager for keypresses
     * @param trigger The key to trigger opening the console
     * @param commandRunner The command runner (what happens once a command has been inputted)
     */
    public void initKeys(InputManager man, KeyTrigger trigger, CommandRunner commandRunner)
    {
        // Adds a key mapping and sets this as a listener
        man.addMapping("console", trigger);
        man.addListener(this, "console");

        // Updates the object that shows how to run commands
        this.commandRunner = commandRunner;
    }


    /**
     * Makes a pop up window show up that allows the user to enter in text.
     * This halts the game, unless you put it on a seperate thread.
     * @param prompt The prompt for the console.
     */
    public void show(String prompt)
    {

        String cmd = (String)JOptionPane.showInputDialog(
                    null, // There is no parent component as we are not making a whole GUI
                    prompt, // This is the text that shows up
                    "Game Console", // This is the title of the window
                    JOptionPane.QUESTION_MESSAGE); // This just makes a "?" icon show up.

        // Runs the command as specified by the 'command runner'
        commandRunner.runCommand(cmd);
    }

    /**
     * Invoked when an action occurs.
     * @param name The name of the action.
     * @param isPressed Is it a press (true), or a release (false)?
     * @param tpf The time between each frame.
     */
    @Override
    public void onAction(String name, boolean isPressed, float tpf)
    {
        // When the trigger key is pressed, show the console.
        if(isPressed)
        {
            show(prompt);
        }
    }

    public static interface CommandRunner
    {
        public void runCommand(String cmd);
    }
}
