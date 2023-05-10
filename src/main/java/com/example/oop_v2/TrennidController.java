package com.example.oop_v2;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.*;

public class TrennidController {
    // Trenni isenditele vajaminevad väljad:
    @FXML
    private TextField trenniNimi;
    @FXML
    private TextField trenniKuupäev;
    @FXML
    private TextField trenniKestvus;
    @FXML
    private TextField trenniSisu;
    //trennide kuvamine:
    @FXML
    private ListView<String> kõikideTrennideSisu;
    //kõik nupud:
    @FXML
    private Button lisa;
    @FXML
    private Button vaata;
    @FXML
    private Button statistika;
    @FXML
    private Button back;

    // statistikaga seotud muutujad
    @FXML
    private BarChart<String, Integer> kuusKuud;
    @FXML
    private CategoryAxis trennidKuusX;
    @FXML
    private NumberAxis trennidKuusY;
    @FXML
    private LineChart<String, Number> kuudeTrenniKestvused;
    @FXML
    private CategoryAxis kuudeTrenniKestvusedX;

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
        //kui mingi vajalik väli on täitmata anname sellest teada
        if (trenniNimi.getText().isEmpty() || trenniKestvus.getText().isEmpty() || trenniKuupäev.getText().isEmpty()){
            sõnum.setText("Mingi väli on jäänud täitmata!");
            return;
        }

        // peidame probleemi tagasiside tekstid ära enne nende vajaminemist
        this.kuupäevProbleemTekst.setVisible(false);
        this.kestvusProbleemTekst.setVisible(false);

        //probleem kuupäevaga:
        LocalDate kuupäev;
        try {
            kuupäev = LocalDate.parse(trenniKuupäev.getText());
        } catch(DateTimeParseException e) {
            this.kuupäevProbleemTekst.setText("Kuupäev ei ole sisestatud õigesti. Kasutage formaati 2023-01-01 (aasta, kuu, päev)");
            this.kuupäevProbleemTekst.setVisible(true);
            return;
        }
        //probleem kestvusega:
        double kestvus;
        try {
            kestvus = Double.parseDouble(trenniKestvus.getText());
        } catch(NumberFormatException e) {
            this.kestvusProbleemTekst.setText("Kestvus peab olema täisarv.");
            this.kestvusProbleemTekst.setVisible(true);
            return;
        }
        //loome uue Trenni isendi ja KõikTrennid isendi
        //selleks loeme failist kõik trennid sisse, lisame just loodud trenni ja kirjutame uuesti faili
        //failis on trennid kuu järgi sorteeritud sel juhul
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
     * VAATA nupp: kui soovitakse vaadata trenide sisu
     */
    @FXML
    public void onVaataButtonPressed(ActionEvent event) throws IOException {
        //muudame stseeni
        FXMLLoader loader = new FXMLLoader(getClass().getResource("trennideKuvamine.fxml"));
        Parent root = loader.load();
        TrennidController controller = loader.getController();


        Scene scene = new Scene(root, 600, 600);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        resize(stage, scene);

        //loeme kõik trennid sisse ning paneme ListViewi
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


    /**
     * meetod tekitab listview-le sius
     */
    public void initializeListView(String[] sisu) {
        kõikideTrennideSisu.setItems(FXCollections.observableArrayList(sisu));
    }
    /**
     * STATISTIKA nupp: soovitakse vaadata trennide statistikat
     */
    @FXML
    public void onStatistikaButtonPressed(ActionEvent event) throws IOException {
        //muudame steeni
        FXMLLoader loader = new FXMLLoader(getClass().getResource("statistika.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root, 1100, 600);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        resize(stage, scene);

        TrennidController controller = loader.getController();
        KõikTrennid kõik=new KõikTrennid(kõikTrennid());
        controller.laeTrennideArvuDiagramm(kõik);
        controller.laeKeskmiseKestvuseDiagramm(kõik);
    }

    /**
     * tekitame graafikule sisu
     */
    public void laeTrennideArvuDiagramm(KõikTrennid trennid) {
        //sorteerime trennid kuupäeva järgi
        SortedSet<LocalDate> sorteeritud = new TreeSet<>(trennid.getTrennideMap().keySet());
        //siia kogume trennid ja nende arvu
        Map<YearMonth, Integer> treeninguteArv = new LinkedHashMap<>();

        YearMonth praeguneKuu = YearMonth.from(LocalDate.now());
        YearMonth kuusKuudTagasi = praeguneKuu.minusMonths(6);

        //kui trenn toimus viimase kuu vahemikus siis lisame selle treeninguteArvu
        for (LocalDate kp:sorteeritud) {
            YearMonth yearMonth = YearMonth.from(kp);
            if (yearMonth.isBefore(kuusKuudTagasi)) {
                continue;
            }
            treeninguteArv.put(yearMonth,  treeninguteArv.getOrDefault(yearMonth, 0)+trennid.getTrennideMap().get(kp).size());
        }

        //igale kuule loome oma tulba
        XYChart.Series<String, Integer> set=new XYChart.Series<>();
        for (YearMonth yearMonth : treeninguteArv.keySet()) {
            set.getData().add(new XYChart.Data<>(yearMonth.toString(), treeninguteArv.getOrDefault(yearMonth,0)));
        }
        trennidKuusX.setLayoutX(50);
        kuusKuud.getData().add(set);
    }

    public void laeKeskmiseKestvuseDiagramm(KõikTrennid trennid) {
        LocalDate praeguneKuupäev = LocalDate.now();
        LocalDate muutuvKuu = LocalDate.of(praeguneKuupäev.getYear(), praeguneKuupäev.getMonth(), 1);

        HashMap<LocalDate, List<Double>> viimasedKuusKuud = new HashMap<>();
        for (int i = 0; i < 6; i++) {
            viimasedKuusKuud.put(muutuvKuu, new ArrayList<>());
            muutuvKuu = muutuvKuu.minusMonths(1);
        }

        // siin vaja teha veel filtreerimine kuude kaupa, et saaks keskmiste statistikat teha
        for(LocalDate trenniKuupäev : trennid.getTrennideMap().keySet()) {
            LocalDate trenniKuu = LocalDate.of(trenniKuupäev.getYear(), trenniKuupäev.getMonth(), 1);
            if(!viimasedKuusKuud.containsKey(trenniKuu))
                continue;

            // lisab trennide kestvused viimase kuue kuu hashmapi mingi kindla kuu juurde
            for(Trenn trenn : trennid.getTrennideMap().get(trenniKuupäev))
                viimasedKuusKuud.get(trenniKuu).add(trenn.getKestvus());
        }

        // loob charti jaoks keskmiste aegade andmed
        XYChart.Series<String, Number> andmed = new XYChart.Series<>();
        for(LocalDate kuu : viimasedKuusKuud.keySet()) {

            // arvutab keskmise 1 kuu jaoks
            double kestvusedKokku = 0;
            int trenneKokku = viimasedKuusKuud.get(kuu).size();
            for(double kestvus : viimasedKuusKuud.get(kuu))
                kestvusedKokku += kestvus;

            double keskmine = kestvusedKokku / trenneKokku;

            andmed.getData().add(new XYChart.Data<>(kuu.toString(), keskmine));
        }

        // paneb andmed joonediagrammi
        this.kuudeTrenniKestvusedX.setLayoutX(50.0);
        this.kuudeTrenniKestvused.getData().add(andmed);
    }

    /**
     * TAGASI nupp:viib tagasi
     */
    @FXML
    public void onBackButtonPressed(ActionEvent event) throws IOException{
        //uus stseen
        Parent root= FXMLLoader.load(getClass().getResource("trennid.fxml"));
        Stage stage=(Stage)((Node)event.getSource()).getScene().getWindow();
        Scene scene=new Scene(root, 600, 600);
        resize(stage, scene);
    }

    /**
     * meetod mis paneb stseeni keset kasutaja ekraani, sest erinevad steenid on eri suurusega
     */
    static void resize(Stage stage, Scene scene) {
        stage.setScene(scene);
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        double centerX = screenBounds.getMinX() + screenBounds.getWidth() / 2;
        double centerY = screenBounds.getMinY() + screenBounds.getHeight() / 2;
        double stageX = centerX - scene.getWidth() / 2;
        double stageY = centerY - scene.getHeight() / 2;
        stage.setX(stageX);
        stage.setY(stageY);
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