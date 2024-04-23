import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import org.json.JSONObject;

public class CurrencyConverterApp {
    private static final String API_KEY = "6ad3aeccf9f22d3b16f2d9e2";
    private static final String BASE_URL = "https://v6.exchangerate-api.com/v6/";

    public static void main(String[] args) throws IOException, InterruptedException {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            mostrarMenu(scanner);

            int option = scanner.nextInt();
            scanner.nextLine(); // Consumir salto de línea

            switch (option) {
                case 1:
                    convertirMoneda(scanner);
                    break;
                case 2:
                    mostrarTiposCambio();
                    break;
                case 3:
                    System.out.println("Saliendo del conversor...");
                    System.exit(0);
                default:
                    System.out.println("Opción inválida. Intenta de nuevo.");
            }
        }
    }

    private static void mostrarMenu(Scanner scanner) {
        System.out.println("\n**Conversor de Monedas**");
        System.out.println("1. Convertir moneda");
        System.out.println("2. Mostrar tipos de cambio");
        System.out.println("3. Salir");
        System.out.print("Elige una opción: ");
    }

    private static void convertirMoneda(Scanner scanner) throws IOException, InterruptedException {
        System.out.print("Ingresa la moneda base (ARS, BOB, BRL, CLP, COP, USD): ");
        String baseCurrency = scanner.nextLine().toUpperCase();

        System.out.print("Ingresa la moneda objetivo (ARS, BOB, BRL, CLP, COP, USD): ");
        String targetCurrency = scanner.nextLine().toUpperCase();

        if (!esMonedaDeseada(baseCurrency) || !esMonedaDeseada(targetCurrency)) {
            System.out.println("Moneda no válida. Las monedas permitidas son: ARS, BOB, BRL, CLP, COP, USD");
            return;
        }

        System.out.print("Ingresa el monto a convertir: ");
        double amount = scanner.nextDouble();

        Map<String, Double> exchangeRates = obtenerTiposCambio(baseCurrency);

        if (exchangeRates == null) {
            System.out.println("Error al obtener los tipos de cambio. Intente de nuevo más tarde.");
            return;
        }

        double convertedAmount = convertCurrency(baseCurrency, targetCurrency, amount, exchangeRates);
        System.out.println(amount + " " + baseCurrency + " equivale a " + convertedAmount + " " + targetCurrency);
    }

    private static Map<String, Double> obtenerTiposCambio(String baseCurrency) throws IOException, InterruptedException {
        HttpClient httpClient = HttpClient.newHttpClient();
        String url = BASE_URL + API_KEY + "/latest/" + baseCurrency;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            JSONObject jsonResponse = new JSONObject(response.body());
            JSONObject rates = jsonResponse.getJSONObject("conversion_rates");

            Map<String, Double> exchangeRates = new HashMap<>();
            for (String currency : rates.keySet()) {
                if (esMonedaDeseada(currency)) {
                    exchangeRates.put(currency, rates.getDouble(currency));
                }
            }

            return exchangeRates;
        } else {
            System.out.println("Error al obtener tipos de cambio: " + response.statusCode());
            return null;
        }
    }

    private static boolean esMonedaDeseada(String currency) {
        // Códigos de moneda deseados: ARS, BOB, BRL, CLP, COP, USD
        return currency.equals("ARS") || currency.equals("BOB") || currency.equals("BRL") ||
                currency.equals("CLP") || currency.equals("COP") || currency.equals("USD");
    }

    private static void mostrarTiposCambio() throws IOException, InterruptedException {
        Map<String, Double> exchangeRates = obtenerTiposCambio("USD");

        if (exchangeRates == null) {
            System.out.println("Error al obtener los tipos de cambio. Intente de nuevo más tarde.");
            return;
        }

        System.out.println("\n**Tipos de cambio para USD:**");
        for (Map.Entry<String, Double> entry : exchangeRates.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
    }

    private static double convertCurrency(String baseCurrency, String targetCurrency, double amount, Map<String, Double> exchangeRates) {
        if (!exchangeRates.containsKey(baseCurrency) || !exchangeRates.containsKey(targetCurrency)) {
            throw new IllegalArgumentException("Monedas no válidas: " + baseCurrency + " o " + targetCurrency);
        }

        double baseRate = exchangeRates.get(baseCurrency);
        double targetRate = exchangeRates.get(targetCurrency);

        return amount * (targetRate / baseRate);
    }
}
