package com.example.oop_v2;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.*;

public class TrennidController {
    private Stage stage;
    private Scene scene;
    private Parent root;
    @FXML
    private TextField trenniNimi;
    @FXML
    private TextField trenniKuupäev;
    @FXML
    private TextField trenniKestvus;
    @FXML
    private TextField trenniSisu;
    @FXML
    private ListView<String> kõikideTrennideSisu;
    @FXML
    private Button lisa;
    @FXML
    private Button vaata;
    @FXML
    private Button statistika;

    // statistika ekraani nupud
    @FXML
    private Button back;
    @FXML
    private BarChart<String, Integer> kuusKuud;
    @FXML
    private CategoryAxis trennidKuusX;
    @FXML
    private NumberAxis trennidKuusY;

    // trenni lisamisel tekkinud errorite tagasiside tekstid
    @FXML
    private Text sõnum;
    @FXML
    private Text kuupäevProbleemTekst;
    @FXML
    private Text kestvusProbleemTekst;

    /**
     * LISA nupule vajutamine: teeb formist saadud info trenni objektiks ja kirjutab selle faili
     */
    @FXML
    protected void onlisaButtonClick() throws IOException {
        if (trenniNimi.getText().isEmpty() || trenniKestvus.getText().isEmpty() || trenniKuupäev.getText().isEmpty()){
            sõnum.setText("Mingi väli on jäänud täitmata!");
            return;
        }

        // peidame probleemi tagasiside tekstid ära enne nende vajaminemist
        this.kuupäevProbleemTekst.setVisible(false);
        this.kestvusProbleemTekst.setVisible(false);

        LocalDate kuupäev;
        try {
            kuupäev = LocalDate.parse(trenniKuupäev.getText());
        } catch(DateTimeParseException e) {
            this.kuupäevProbleemTekst.setText("Kuupäev ei ole sisestatud õigesti. Kasutage formaati 2023-01-01 (aasta, kuu, päev)");
            this.kuupäevProbleemTekst.setVisible(true);
            return;
        }

        double kestvus;
        try {
            kestvus = Double.parseDouble(trenniKestvus.getText());
        } catch(NumberFormatException e) {
            this.kestvusProbleemTekst.setText("Kestvus peab olema täisarv.");
            this.kestvusProbleemTekst.setVisible(true);
            return;
        }

        Trenn uusTrenn=new Trenn(trenniNimi.getText(), kuupäev, kestvus, trenniSisu.getText());

        KõikTrennid kõikTrennid = new KõikTrennid(kõikTrennid());
        if (!kõikTrennid.getTrennideMap().containsKey(kuupäev)) {
            kõikTrennid.getTrennideMap().put(kuupäev, new ArrayList<>());
        }
        kõikTrennid.getTrennideMap().get(kuupäev).add(uusTrenn);
        kirjutaFaili(kõikTrennid);

        trenniNimi.setText("");
        trenniKuupäev.setText("");
        trenniKestvus.setText("");
        trenniSisu.setText("");
        sõnum.setText("Trenn lisatud!");
    }

    /**
     * kui soovitakse vaadata trenide sisu
     */
    @FXML
    public void onVaataButtonPressed(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("trennideKuvamine.fxml"));
        Parent root = loader.load();
        TrennidController controller = loader.getController();

        Scene scene = new Scene(root);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();

        KõikTrennid kõikTrennid = new KõikTrennid(kõikTrennid());
        ArrayList<String> kirjuta = new ArrayList<>();
        for (ArrayList<Trenn> trennideList : kõikTrennid.getTrennideMap().values()) {
            for (Trenn trenn : trennideList) {
                kirjuta.add(trenn.getNimetus()+", kuupäeval "+trenn.getKuupöev()+", kestvus: "+trenn.getKestvus()+", sisuga: "+trenn.getSisu());
            }
        }
        String[] strings = kirjuta.toArray(String[]::new);
        controller.initializeListView(strings);
    }
    public void initializeListView(String[] sisu) {
        kõikideTrennideSisu.setItems(FXCollections.observableArrayList(sisu));
    }
    @FXML
    public void onStatistikaButtonPressed(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("statistika.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();

        TrennidController controller = loader.getController();
        KõikTrennid kõik=new KõikTrennid(kõikTrennid());
        controller.initializeChart(kõik);
    }
    public void initializeChart(KõikTrennid trennid) {
        SortedSet<LocalDate> sorted = new TreeSet<>(trennid.getTrennideMap().keySet());
        Map<YearMonth, Integer> trainingsByMonth = new LinkedHashMap<>();

        YearMonth currentDate = YearMonth.from(LocalDate.now());
        YearMonth sixMonthsAgo = currentDate.minusMonths(6);

        for (LocalDate kp:sorted) {
            YearMonth yearMonth = YearMonth.from(kp);
            if (yearMonth.isBefore(sixMonthsAgo)) {
                continue; // skip dates outside the last 6 months range
            }
            trainingsByMonth.put(yearMonth,  trainingsByMonth.getOrDefault(yearMonth, 0)+trennid.getTrennideMap().get(kp).size());
        }

        XYChart.Series<String, Integer> set=new XYChart.Series<>();
        for (YearMonth yearMonth : trainingsByMonth.keySet()) {
            set.getData().add(new XYChart.Data<>(yearMonth.toString(), trainingsByMonth.getOrDefault(yearMonth,0)));
        }
        trennidKuusX.setLayoutX(50);
        kuusKuud.getData().add(set);
    }

    public void laeKeskmiseKestvuseDiagramm(KõikTrennid trennid) {
        LocalDate praeguneKuupäev = LocalDate.now();
        LocalDate muutuvKuu = LocalDate.of(praeguneKuupäev.getYear(), praeguneKuupäev.getMonth(), 1);

        HashMap<LocalDate, List<Integer>> viimasedKuusKuud = new HashMap<>();
        for (int i = 0; i < 6; i++) {
            viimasedKuusKuud.put(muutuvKuu, new ArrayList<>());
            muutuvKuu = muutuvKuu.minusMonths(1);
        }

        // siin vaja teha veel filtreerimine kuude kaupa, et saaks keskmiste statistikat teha
    }

    /**
     * viib tagasi
     */
    @FXML
    public void onBackButtonPressed(ActionEvent event) throws IOException{
        Parent root= FXMLLoader.load(getClass().getResource("trennid.fxml"));
        stage=(Stage)((Node)event.getSource()).getScene().getWindow();
        scene=new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * loeb failist mappi
     */
    public Map<LocalDate, ArrayList<Trenn>> kõikTrennid() throws IOException {
        Map<LocalDate, ArrayList<Trenn>> tulemus=new HashMap<>();
        try (BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream("Trennid.txt"), "UTF-8"))){
            String rida;
            while((rida=br.readLine())!=null){
                String[] osad=rida.split(";");
                LocalDate kp=LocalDate.parse(osad[1]);
                Trenn trenn;
                if (osad.length<4) trenn=new Trenn(osad[0], kp, Double.parseDouble(osad[2]), "");
                else trenn=new Trenn(osad[0], kp, Double.parseDouble(osad[2]), osad[3]);
                if (!tulemus.containsKey(kp)) {
                    tulemus.put(kp, new ArrayList<>());
                }
                tulemus.get(kp).add(trenn);
            }
        }
        return tulemus;
    }

    /**
     * kirjutab faili kõik mingi KõikTrennid välja kõikTrennid sisu
     */
    public void kirjutaFaili(KõikTrennid kõikTrennid) throws IOException {
        try(BufferedWriter bw=new BufferedWriter(new OutputStreamWriter(new FileOutputStream("trennid.txt"), "UTF-8"))){
            for (ArrayList<Trenn> trennideList : kõikTrennid.getTrennideMap().values()) {
                for (Trenn trenn : trennideList) {
                    bw.write(trenn.toString());
                    bw.newLine();
                }
            }
        }
    }
}