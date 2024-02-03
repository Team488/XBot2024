package competition.subsystems;

import edu.wpi.first.wpilibj.util.Color8Bit;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import xbot.common.advantage.DataFrameRefreshable;
import xbot.common.command.BaseSubsystem;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

/**
 * This class allows the robot to publish NeoTrellis button states
 * to NetworkTables for visualization on the gamepad.
 */
@Singleton
public class NeoTrellisGamepadSubsystem extends BaseSubsystem implements DataFrameRefreshable {

    final List<Color8Bit> buttonColors;

    /**
     * Create a new NeoTrellisGamepadSubsystem
     */
    @Inject
    public NeoTrellisGamepadSubsystem() {
        this.buttonColors = new ArrayList<>(32);
        for (int i = 0; i < 32; i++) {
            buttonColors.add(new Color8Bit(0, 0, 0));
        }
    }

    /**
     * Set the color of a button on the gamepad
     * @param buttonIndex The index of the button to set
     * @param color The color to set the button to
     */
    public void setButtonColor(int buttonIndex, Color8Bit color) {
        buttonColors.set(buttonIndex, color);
    }

    /**
     * Clear the colors of all buttons on the gamepad
     */
    public void clearButtonColors() {
        for (int i = 0; i < 32; i++) {
            buttonColors.set(i, new Color8Bit(0, 0, 0));
        }
    }

    /**
     * Fill the colors of all buttons on the gamepad with a single color
     * @param color The color to fill the buttons with
     */
    public void fillButtonColors(Color8Bit color) {
        for (int i = 0; i < 32; i++) {
            buttonColors.set(i, color);
        }
    }

    @Override
    public void refreshDataFrame() {
        aKitLog.record("ButtonColors",
                buttonColors.stream().map(Color8Bit::toHexString).toArray(String[]::new));
    }

    /**
     * Get a command to clear the button colors
     * @return A command to clear the button colors
     */
    public Command getClearCommand() {
        return new InstantCommand(this::clearButtonColors);
    }

    /**
     * Get a command to fill the button colors with a single color
     * @param color The color to fill the buttons with
     * @return A command to fill the button colors with a single color
     */
    public Command getFillCommand(Color8Bit color) {
        return new InstantCommand(() -> fillButtonColors(color));
    }

    /**
     * Get a command to set the color of a button
     * @param buttonIndex The index of the button to set
     * @param color The color to set the button to
     * @return A command to set the color of a button
     */
    public Command getSetButtonColorCommand(int buttonIndex, Color8Bit color) {
        return new InstantCommand(() -> setButtonColor(buttonIndex, color));
    }
}
