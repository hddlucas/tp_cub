package com.example.user_admin.sensores;

import android.content.Context;

import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.J48;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;
import weka.core.Instances;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.example.user_admin.sensores.MainActivity.ACTIVITIES;
import static com.example.user_admin.sensores.MainActivity.ARFFCSVFILENAME;
import static com.example.user_admin.sensores.MainActivity.ARFFFILENAME;
import static com.example.user_admin.sensores.MainActivity.FILTERED_NOISE_ARFFCSVFILENAME;
import static com.example.user_admin.sensores.MainActivity.FILTERED_NOISE_ARFFFILENAME;
import static com.example.user_admin.sensores.MainActivity.TEST_ARFFCSVFILENAME;
import static com.example.user_admin.sensores.MainActivity.TRAIN_ARFFCSVFILENAME;

public class Arff {

    private Utils utils;
    private Context context;
    private FileManager fileManager;
    private int N = 64;

    public Arff(Context context) {
        this.context = context;
    }

    public void convertCSVtoArff(String csvFile, String arffFile) {

        try {
            // load CSV
            CSVLoader loader = new CSVLoader();
            loader.setSource(new File(context.getFilesDir() + "/" + csvFile));
            //String[] options = {"-H"};
            //loader.setOptions(options);

            Instances data = loader.getDataSet();

            // save ARFF
            ArffSaver saver = new ArffSaver();
            saver.setInstances(data);
            saver.setFile(new File(context.getFilesDir() + "/" + arffFile));
            saver.setDestination(new File(context.getFilesDir() + "/" + arffFile));
            saver.writeBatch();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public String classifyFromARFF(String train_arff,String test_arff) throws Exception {//função classifyFromARFF
        FilteredClassifier fc = new FilteredClassifier();

        BufferedReader reader = new BufferedReader(new FileReader(context.getFilesDir() + "/" +train_arff));
        Instances train = new Instances(reader); //leitura do ficheiro .arff
        train.setClassIndex(train.numAttributes() - 1);

        // classifier
        J48 j48 = new J48(); //utilização do algoritmo J48
        j48.setUnpruned(true);
        fc = new FilteredClassifier(); //criação do classificador
        fc.setClassifier(j48);
        fc.buildClassifier(train);

        //função predictActivity
        reader = new BufferedReader(new FileReader(context.getFilesDir() + "/" +test_arff));
        Instances test = new Instances(reader); //leitura do ficheiro .arff de testes
        test.setClassIndex(test.numAttributes() - 1);
        double pred=-0.0;
        for (int i = 0; i < test.numInstances(); i++) {
            pred = fc.classifyInstance(test.instance(i)); //classificação
            //System.out.println(test.classAttribute().value((int) pred)); //determinação da atividade
        }

        return test.classAttribute().value((int) pred);



    }


    public void generateArffFile(boolean filter_noise) {
        utils = new Utils(context);
        fileManager = new FileManager(context);

        if (!filter_noise) {
            fileManager.deleteFile(ARFFCSVFILENAME);
            fileManager.deleteFile(ARFFFILENAME);
        } else {
            fileManager.deleteFile(FILTERED_NOISE_ARFFCSVFILENAME);
            fileManager.deleteFile(FILTERED_NOISE_ARFFFILENAME);
        }

        String filename = filter_noise == false ? "treino.csv" : "avg.csv";
        List<String[]> rows = new ArrayList<>();
        try {
            FileManager csvReader = new FileManager(context);
            rows = csvReader.readCSV(filename);

        } catch (IOException e) {
            e.printStackTrace();
        }

        FFT fft = new FFT(N);
        String fileHeader = "";
        int i;
        for (i = 1; i <= N; i++) {
            fileHeader += ("accelerometer" + i + ",");
        }
        for (i = 1; i <= N; i++) {
            fileHeader += ("gyroscope" + i + ",");
        }
        for (i = 1; i <= N; i++) {
            fileHeader += ("gravity" + i + ",");
        }
        fileHeader += ("activity\n");

        double acc_sqrt, gyro_sqrt, grav_sqrt;
        for (int a = 0; a < ACTIVITIES.length; a++) {
            String fft_complex = "";
            double[] window = fft.getWindow();
            //acc
            double[] re_acc = new double[N];
            double[] im_acc = new double[N];
            //gyro
            double[] re_gyro = new double[N];
            double[] im_gyro = new double[N];
            //grav
            double[] re_grav = new double[N];
            double[] im_grav = new double[N];
            List<List<String>> fftExcelData = new ArrayList<List<String>>();
            List<String> accValues = new ArrayList<String>();
            List<String> gyroValues = new ArrayList<String>();
            List<String> gravValues = new ArrayList<String>();

            //lines for sensors
            fftExcelData.add(accValues);
            fftExcelData.add(gyroValues);
            fftExcelData.add(gravValues);

            int aux = 0;
            for (int j = 0, x = 0; j < rows.size(); j++) {
                if (j + N > rows.size())
                    break;
                if (rows.get(j)[14].equals(ACTIVITIES[a])) {
                    if (x != 0 && x % N == 0) {
                        //acc
                        fft.beforeAfter(fft, re_acc, im_acc);
                        for (int k = 0; k < re_acc.length; k++) {
                            fft_complex = String.valueOf(re_acc[k]);
                            //FFT complex acc
                            fftExcelData.get(0).add(fft_complex);
                        }
                        //gyro
                        fft.beforeAfter(fft, re_gyro, im_gyro);
                        for (int k = 0; k < re_gyro.length; k++) {
                            fft_complex = String.valueOf(re_gyro[k]);

                            //FFT mag gyro
                            fftExcelData.get(1).add(fft_complex);
                        }
                        //grav
                        fft.beforeAfter(fft, re_grav, im_grav);
                        for (int k = 0; k < re_grav.length; k++) {
                            fft_complex = String.valueOf(re_grav[k]);

                            //FFT complex
                            fftExcelData.get(2).add(fft_complex);
                        }

                        aux = 0;

                        //acc
                        re_acc = new double[N];
                        im_acc = new double[N];

                        //gyro
                        re_gyro = new double[N];
                        im_gyro = new double[N];

                        //grav
                        re_grav = new double[N];
                        im_grav = new double[N];
                    }

                    acc_sqrt = utils.calculateAngularVelocity(Double.parseDouble(rows.get(j)[4]), Double.parseDouble(rows.get(j)[5]), Double.parseDouble(rows.get(j)[6]));
                    gyro_sqrt = utils.calculateAngularVelocity(Double.parseDouble(rows.get(j)[7]), Double.parseDouble(rows.get(j)[8]), Double.parseDouble(rows.get(j)[9]));
                    grav_sqrt = utils.calculateAngularVelocity(Double.parseDouble(rows.get(j)[10]), Double.parseDouble(rows.get(j)[11]), Double.parseDouble(rows.get(j)[12]));

                    re_acc[aux] = acc_sqrt;
                    im_acc[aux] = 0;

                    re_gyro[aux] = gyro_sqrt;
                    im_gyro[aux] = 0;

                    re_grav[aux] = grav_sqrt;
                    im_grav[aux] = 0;
                    aux++;
                    x++;
                }
            }
            //transpose
            List<List<String>> dataArff = new ArrayList<List<String>>();
            int auxCont = 0;
            int aux2 = 0;
            for (int y = 0; y < fftExcelData.get(0).size(); y++) {
                if ((N + aux2) <= fftExcelData.get(0).size()) {
                    dataArff.add(new ArrayList<String>());
                    for (int l = 0; l < 3; l++) {
                        for (int k = 0; k < N; k++) {
                            dataArff.get(auxCont).add(fftExcelData.get(l).get(k + aux2));
                        }
                    }
                    dataArff.get(auxCont).add(ACTIVITIES[a]);
                    aux2 += N;
                    auxCont++;
                }
            }
            fileManager.createFile(context.getFilesDir() + "/" + (filter_noise == true ? FILTERED_NOISE_ARFFCSVFILENAME : ARFFCSVFILENAME), fileHeader);
            FileOutputStream outputStream;
            String s = "";
            try {
                outputStream = context.openFileOutput((filter_noise == true ? FILTERED_NOISE_ARFFCSVFILENAME : ARFFCSVFILENAME), Context.MODE_APPEND);
                Iterator<List<String>> iter = dataArff.iterator();
                while (iter.hasNext()) {
                    Iterator<String> siter = iter.next().iterator();
                    s = "";
                    while (siter.hasNext()) {
                        s += siter.next() + ",";
                    }
                    //remove last comma
                    s = s.substring(0, s.length() - 1);
                    outputStream.write((s + "\n").getBytes());
                }
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            //Convert csv to arff file
            convertCSVtoArff((filter_noise == true ? FILTERED_NOISE_ARFFCSVFILENAME : ARFFCSVFILENAME), (filter_noise == true ? FILTERED_NOISE_ARFFFILENAME : ARFFFILENAME));
        }
    }

    public void generateRealTimeArffFile(List<String[]> rows, String activity,boolean automaticMode) {
        utils = new Utils(context);
        fileManager = new FileManager(context);

        int N = 64;
        FFT fft = new FFT(N);
        String fileHeader = "";
        int i;
        for (i = 1; i <= N; i++) {
            fileHeader += ("accelerometer" + i + ",");
        }
        for (i = 1; i <= N; i++) {
            fileHeader += ("gyroscope" + i + ",");
        }
        for (i = 1; i <= N; i++) {
            fileHeader += ("gravity" + i + ",");
        }
        fileHeader += ("activity\n");

        double acc_sqrt, gyro_sqrt, grav_sqrt;
        String fft_complex = "";
        double[] window = fft.getWindow();
        //acc
        double[] re_acc = new double[N];
        double[] im_acc = new double[N];
        //gyro
        double[] re_gyro = new double[N];
        double[] im_gyro = new double[N];
        //grav
        double[] re_grav = new double[N];
        double[] im_grav = new double[N];
        List<List<String>> fftExcelData = new ArrayList<List<String>>();
        List<String> accValues = new ArrayList<String>();
        List<String> gyroValues = new ArrayList<String>();
        List<String> gravValues = new ArrayList<String>();

        //lines for sensors
        fftExcelData.add(accValues);
        fftExcelData.add(gyroValues);
        fftExcelData.add(gravValues);

        int aux = 0;
        for (int j = 0, x = 0; j < rows.size(); j++) {
            if (j == N - 1) {
                //acc
                fft.beforeAfter(fft, re_acc, im_acc);
                for (int k = 0; k < re_acc.length; k++) {
                    fft_complex = String.valueOf(re_acc[k]);
                    //FFT complex acc
                    fftExcelData.get(0).add(fft_complex);
                }
                //gyro
                fft.beforeAfter(fft, re_gyro, im_gyro);
                for (int k = 0; k < re_gyro.length; k++) {
                    fft_complex = String.valueOf(re_gyro[k]);

                    //FFT mag gyro
                    fftExcelData.get(1).add(fft_complex);
                }
                //grav
                fft.beforeAfter(fft, re_grav, im_grav);
                for (int k = 0; k < re_grav.length; k++) {
                    fft_complex = String.valueOf(re_grav[k]);

                    //FFT complex
                    fftExcelData.get(2).add(fft_complex);
                }
                aux = 0;
                //acc
                re_acc = new double[N];
                im_acc = new double[N];

                //gyro
                re_gyro = new double[N];
                im_gyro = new double[N];

                //grav
                re_grav = new double[N];
                im_grav = new double[N];
            }

            acc_sqrt = utils.calculateAngularVelocity(Double.parseDouble(rows.get(j)[4]), Double.parseDouble(rows.get(j)[5]), Double.parseDouble(rows.get(j)[6]));
            gyro_sqrt = utils.calculateAngularVelocity(Double.parseDouble(rows.get(j)[7]), Double.parseDouble(rows.get(j)[8]), Double.parseDouble(rows.get(j)[9]));
            grav_sqrt = utils.calculateAngularVelocity(Double.parseDouble(rows.get(j)[10]), Double.parseDouble(rows.get(j)[11]), Double.parseDouble(rows.get(j)[12]));

            re_acc[aux] = acc_sqrt;
            im_acc[aux] = 0;

            re_gyro[aux] = gyro_sqrt;
            im_gyro[aux] = 0;

            re_grav[aux] = grav_sqrt;
            im_grav[aux] = 0;
            aux++;
            x++;

        }
        //transpose
        List<List<String>> dataArff = new ArrayList<List<String>>();
        int auxCont = 0;
        int aux2 = 0;
        for (int y = 0; y < fftExcelData.get(0).size(); y++) {
            if ((N + aux2) <= fftExcelData.get(0).size()) {
                dataArff.add(new ArrayList<String>());
                for (int l = 0; l < 3; l++) {
                    for (int k = 0; k < N; k++) {
                        dataArff.get(auxCont).add(fftExcelData.get(l).get(k + aux2));
                    }
                }
                dataArff.get(auxCont).add(activity);
                aux2 += N;
                auxCont++;
            }
        }
        fileManager.createFile(context.getFilesDir() + "/" + (automaticMode ? TEST_ARFFCSVFILENAME : TRAIN_ARFFCSVFILENAME), fileHeader);
        FileOutputStream outputStream;
        String s = "";
        try {
            outputStream = context.openFileOutput((automaticMode ? TEST_ARFFCSVFILENAME : TRAIN_ARFFCSVFILENAME), Context.MODE_APPEND);
            Iterator<List<String>> iter = dataArff.iterator();
            while (iter.hasNext()) {
                Iterator<String> siter = iter.next().iterator();
                s = "";
                while (siter.hasNext()) {
                    s += siter.next() + ",";
                }
                //remove last comma
                s = s.substring(0, s.length() - 1);
                outputStream.write((s + "\n").getBytes());
            }
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}