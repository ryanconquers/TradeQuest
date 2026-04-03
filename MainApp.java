import javax.swing.SwingUtilities;
import ui.SplashScreen;
import ui.LoginScreen;
import ui.MainFrame;
import model.User;

public class MainApp {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {

            // Step 1 — Show Splash Screen
            // When splash completes it calls the lambda below
            new SplashScreen(() -> {

                // Step 2 — Show Login Screen (on EDT)
                SwingUtilities.invokeLater(() -> {
                    new LoginScreen(user -> {

                        // Step 3 — Launch Main App with User from login
                        SwingUtilities.invokeLater(() -> {
                            new MainFrame(user);
                        });

                    });
                });

            });

        });
    }
}