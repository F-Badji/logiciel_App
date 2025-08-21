import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class ImeiCheck {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Entrez un IMEI : ");
        String imei = scanner.nextLine().trim();

        if (imei.length() != 15) {
            System.out.println("IMEI invalide (doit faire 15 chiffres).");
            return;
        }

        String apiKey = "28833799-a5fc-4edb-ba5e-7b8531afed15";
        String service = "2"; // service pour obtenir marque + modèle

        String urlString = String.format("https://api.imei.pro/?key=%s&imei=%s&service=%s", apiKey, imei, service);

        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();
            if(responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while((inputLine = in.readLine()) != null){
                    response.append(inputLine);
                }
                in.close();

                System.out.println("Réponse API : " + response.toString());
            } else {
                System.out.println("Erreur HTTP : " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        scanner.close();
    }
}
