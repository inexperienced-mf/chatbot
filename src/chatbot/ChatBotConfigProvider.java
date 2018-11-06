package chatbot;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

class ChatBotConfigProvider {
    final String telegramToken;
    final String adminPassword;

    private ChatBotConfigProvider(String telegramToken, String adminPassword) {
        this.telegramToken = telegramToken;
        this.adminPassword = adminPassword;
    }

    static ChatBotConfigProvider readConfig(String pathname) {
        try {
            Scanner cfg = new Scanner(new File(pathname));
            String telegramTokenLine = cfg.nextLine();
            String adminPasswordLine = cfg.nextLine();
            if (!telegramTokenLine.startsWith("telegram token: ") ||
                    !adminPasswordLine.startsWith("admin password: "))
                throw new ConfigParseException();
            String telegramToken = telegramTokenLine.substring(16);
            String adminPassword = adminPasswordLine.substring(16);
            return new ChatBotConfigProvider(telegramToken, adminPassword);

        } catch (FileNotFoundException | ConfigParseException e) {
            e.printStackTrace();
        }
        return new ChatBotConfigProvider(null, null);
    }
}
