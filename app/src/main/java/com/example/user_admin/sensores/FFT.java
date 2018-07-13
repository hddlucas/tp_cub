/*
  *  Copyright 2006-2007 Columbia University.
  *
  *  This file is part of MEAPsoft.
  *
  *  MEAPsoft is free software; you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License version 2 as
  *  published by the Free Software Foundation.
  *
  *  MEAPsoft is distributed in the hope that it will be useful, but
  *  WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  *  General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with MEAPsoft; if not, write to the Free Software
  *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  *  02110-1301 USA
  *
  *  See the file "COPYING" for the text of the license.
  */

package com.example.user_admin.sensores;


import android.content.Context;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.example.user_admin.sensores.MainActivity.ACTIVITIES;
import static com.example.user_admin.sensores.MainActivity.FFTFILEHEADER;
import static com.example.user_admin.sensores.MainActivity.FFTFILENAME;
import static com.example.user_admin.sensores.MainActivity.FILTERED_NOISE_FFTFILENAME;

public class FFT {
    private FileManager fileManager;
    private Complex complex;
    private Utils utils;
    private Context context;
    FileManager csvReader = new FileManager(this.context);

    public FFT(Context context) {
        this.context = context;
    }

    int n, m;

    // Lookup tables.  Only need to recompute when size of FFT changes.
    double[] cos;
    double[] sin;

    double[] window;

    public FFT(int n) {
        this.n = n;
        this.m = (int) (Math.log(n) / Math.log(2));

        // Make sure n is a power of 2
        if (n != (1 << m))
            throw new RuntimeException("FFT length must be power of 2");

        // precompute tables
        cos = new double[n / 2];
        sin = new double[n / 2];

        //     for(int i=0; i<n/4; i++) {
        //       cos[i] = Math.cos(-2*Math.PI*i/n);
        //       sin[n/4-i] = cos[i];
        //       cos[n/2-i] = -cos[i];
        //       sin[n/4+i] = cos[i];
        //       cos[n/2+i] = -cos[i];
        //       sin[n*3/4-i] = -cos[i];
        //       cos[n-i]   = cos[i];
        //       sin[n*3/4+i] = -cos[i];
        //     }


        for (int i = 0; i < n / 2; i++) {
            cos[i] = Math.cos(-2 * Math.PI * i / n);
            sin[i] = Math.sin(-2 * Math.PI * i / n);
        }

        makeWindow();
    }

    protected void makeWindow() {
        // Make a blackman window:
        // w(n)=0.42-0.5cos{(2*PI*n)/(N-1)}+0.08cos{(4*PI*n)/(N-1)};
        window = new double[n];
        for (int i = 0; i < window.length; i++)
            window[i] = 0.42 - 0.5 * Math.cos(2 * Math.PI * i / (n - 1))
                    + 0.08 * Math.cos(4 * Math.PI * i / (n - 1));
    }

    public double[] getWindow() {
        return window;
    }


    /***************************************************************
     * fft.c
     * Douglas L. Jones
     * University of Illinois at Urbana-Champaign
     * January 19, 1992
     * http://cnx.rice.edu/content/m12016/latest/
     *
     *   fft: in-place radix-2 DIT DFT of a complex input
     *
     *   input:
     * n: length of FFT: must be a power of two
     * m: n = 2**m
     *   input/output
     * x: double array of length n with real part of data
     * y: double array of length n with imag part of data
     *
     *   Permission to copy and use this program is granted
     *   as long as this header is included.
     ****************************************************************/
    public void fft(double[] x, double[] y) {
        int i, j, k, n1, n2, a;
        double c, s, e, t1, t2;


        // Bit-reverse
        j = 0;
        n2 = n / 2;
        for (i = 1; i < n - 1; i++) {
            n1 = n2;
            while (j >= n1) {
                j = j - n1;
                n1 = n1 / 2;
            }
            j = j + n1;

            if (i < j) {
                t1 = x[i];
                x[i] = x[j];
                x[j] = t1;
                t1 = y[i];
                y[i] = y[j];
                y[j] = t1;
            }
        }

        // FFT
        n1 = 0;
        n2 = 1;

        for (i = 0; i < m; i++) {
            n1 = n2;
            n2 = n2 + n2;
            a = 0;

            for (j = 0; j < n1; j++) {
                c = cos[a];
                s = sin[a];
                a += 1 << (m - i - 1);

                for (k = j; k < n; k = k + n2) {
                    t1 = c * x[k + n1] - s * y[k + n1];
                    t2 = s * x[k + n1] + c * y[k + n1];
                    x[k + n1] = x[k] - t1;
                    y[k + n1] = y[k] - t2;
                    x[k] = x[k] + t1;
                    y[k] = y[k] + t2;
                }
            }
        }
    }

    public  void beforeAfter(FFT fft, double[] re, double[] im) {
        System.out.println("Before: ");
        printReIm(re, im);
        fft.fft(re, im);
        System.out.println("After: ");
        printReIm(re, im);
    }

    public  void printReIm(double[] re, double[] im) {
        System.out.print("Re: [");
        for (int i = 0; i < re.length; i++)
            System.out.print(((int) (re[i] * 1000) / 1000.0) + " ");

        System.out.print("]\nIm: [");
        for (int i = 0; i < im.length; i++)
            System.out.print(((int) (im[i] * 1000) / 1000.0) + " ");

        System.out.println("]");
    }

    public void generateFourierTransform(boolean filter_noise) {
        fileManager = new FileManager(context);
        if(!filter_noise)
            fileManager.deleteFile(FFTFILENAME);
        else
            fileManager.deleteFile(FILTERED_NOISE_FFTFILENAME);

        utils = new Utils(context);
        List<String[]> rows = new ArrayList<>();

        String filename= filter_noise== false ? "treino.csv" : "avg.csv";
        try {
            FileManager csvReader = new FileManager(context);
            rows = csvReader.readCSV(filename);

        } catch (IOException e) {
            e.printStackTrace();
        }

        int N = 64;
        FFT fft = new FFT(N);

        for (int a = 0; a < ACTIVITIES.length; a++) {
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

            String fft_complex = "";

            double acc_sqrt, gyro_sqrt, grav_sqrt;

            int aux = 0;
            ArrayList fft_complex_array = new ArrayList<String>();
            List<List<String>> fftExcelData = new ArrayList<List<String>>();
            List<String> timestamp = new ArrayList<String>();

            List<String> data_acc = new ArrayList<String>();
            List<String> fft_freq_acc = new ArrayList<String>();
            List<String> serie_acc = new ArrayList<String>();
            List<String> fft_mag_acc = new ArrayList<String>();
            List<String> fft_complex_acc = new ArrayList<String>();

            List<String> data_gyro = new ArrayList<String>();
            List<String> fft_freq_gyro = new ArrayList<String>();
            List<String> serie_gyro = new ArrayList<String>();
            List<String> fft_mag_gyro = new ArrayList<String>();
            List<String> fft_complex_gyro = new ArrayList<String>();

            List<String> data_grav = new ArrayList<String>();
            List<String> fft_freq_grav = new ArrayList<String>();
            List<String> serie_grav = new ArrayList<String>();
            List<String> fft_mag_grav = new ArrayList<String>();
            List<String> fft_complex_grav = new ArrayList<String>();
            List<String> current_activity = new ArrayList<String>();

            //acc
            fftExcelData.add(timestamp);
            fftExcelData.add(data_acc);
            fftExcelData.add(fft_freq_acc);
            fftExcelData.add(serie_acc);
            fftExcelData.add(fft_mag_acc);
            fftExcelData.add(fft_complex_acc);

            //gyro
            fftExcelData.add(data_gyro);
            fftExcelData.add(fft_freq_gyro);
            fftExcelData.add(serie_gyro);
            fftExcelData.add(fft_mag_gyro);
            fftExcelData.add(fft_complex_gyro);

            //grav
            fftExcelData.add(data_grav);
            fftExcelData.add(fft_freq_grav);
            fftExcelData.add(serie_grav);
            fftExcelData.add(fft_mag_grav);
            fftExcelData.add(fft_complex_grav);
            fftExcelData.add(current_activity);
            for (int j = 0, x = 0; j < rows.size(); j++) {
                //stop cycle
                if (j + N > rows.size())
                    break;

                if (rows.get(j)[14].equals(ACTIVITIES[a])) {
                    if (x != 0 && x % N == 0) {
                        //acc
                        fft.beforeAfter(fft, re_acc, im_acc);
                        for (int k = 0; k < re_acc.length; k++) {
                            if (im_acc[k] > 0)
                                fft_complex = String.valueOf(re_acc[k]) + "+" + String.valueOf(im_acc[k]) + "i";
                            else if (im_acc[k] < 0)
                                fft_complex = String.valueOf(re_acc[k]) + String.valueOf(im_acc[k]) + "i";
                            else
                                fft_complex = String.valueOf(re_acc[k]);

                            //fft_complex_array.add(fft_complex);
                            complex = new Complex(re_acc[k], im_acc[k]);

                            //FFT mag acc
                            fftExcelData.get(4).add(String.valueOf((double) 2 / N * complex.abs()));

                            //FFT complex acc
                            fftExcelData.get(5).add(fft_complex);
                        }
                        //gyro
                        fft.beforeAfter(fft, re_gyro, im_gyro);
                        for (int k = 0; k < re_gyro.length; k++) {
                            if (im_gyro[k] > 0)
                                fft_complex = String.valueOf(re_gyro[k]) + "+" + String.valueOf(im_gyro[k]) + "i";
                            else if (im_gyro[k] < 0)
                                fft_complex = String.valueOf(re_gyro[k]) + String.valueOf(im_gyro[k]) + "i";
                            else
                                fft_complex = String.valueOf(re_gyro[k]);

                            //fft_complex_array.add(fft_complex);
                            complex = new Complex(re_gyro[k], im_gyro[k]);

                            //FFT mag gyro
                            fftExcelData.get(9).add(String.valueOf((double) 2 / N * complex.abs()));

                            //FFT complex gyro
                            fftExcelData.get(10).add(fft_complex);
                        }
                        //grav
                        fft.beforeAfter(fft, re_grav, im_grav);
                        for (int k = 0; k < re_grav.length; k++) {
                            if (im_grav[k] > 0)
                                fft_complex = String.valueOf(re_grav[k]) + "+" + String.valueOf(im_grav[k]) + "i";
                            else if (im_grav[k] < 0)
                                fft_complex = String.valueOf(re_grav[k]) + String.valueOf(im_grav[k]) + "i";
                            else
                                fft_complex = String.valueOf(re_grav[k]);

                            //fft_complex_array.add(fft_complex);
                            complex = new Complex(re_grav[k], im_grav[k]);

                            //FFT mag grav
                            fftExcelData.get(14).add(String.valueOf((double) 2 / N * complex.abs()));

                            //FFT complex grav
                            fftExcelData.get(15).add(fft_complex);
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

                    //timestamp
                    fftExcelData.get(0).add(rows.get(j)[3]);
                    acc_sqrt = utils.calculateAngularVelocity(Double.parseDouble(rows.get(j)[4]), Double.parseDouble(rows.get(j)[5]), Double.parseDouble(rows.get(j)[6]));
                    gyro_sqrt = utils.calculateAngularVelocity(Double.parseDouble(rows.get(j)[7]), Double.parseDouble(rows.get(j)[8]), Double.parseDouble(rows.get(j)[9]));
                    grav_sqrt = utils.calculateAngularVelocity(Double.parseDouble(rows.get(j)[10]), Double.parseDouble(rows.get(j)[11]), Double.parseDouble(rows.get(j)[12]));

                    //data_acc
                    fftExcelData.get(1).add(String.valueOf(acc_sqrt));
                    //data_gyro
                    fftExcelData.get(6).add(String.valueOf(gyro_sqrt));
                    //data_grav
                    fftExcelData.get(11).add(String.valueOf(grav_sqrt));

                    //FFT freq acc
                    fftExcelData.get(2).add(String.valueOf((double) aux * 308 / N));
                    //FFT freq gyro
                    fftExcelData.get(7).add(String.valueOf((double) aux * 308 / N));
                    //FFT freq grav
                    fftExcelData.get(12).add(String.valueOf((double) aux * 308 / N));

                    //serie acc
                    fftExcelData.get(3).add(String.valueOf(aux));
                    //serie gyro
                    fftExcelData.get(8).add(String.valueOf(aux));
                    //serie grav
                    fftExcelData.get(13).add(String.valueOf(aux));


                    fftExcelData.get(16).add(String.valueOf(ACTIVITIES[a]));

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

            //remove exceeded elements
            int size = timestamp.size();
            if (aux < N + 1) {
                for (int j = 1; j < aux + 1; j++) {
                    timestamp.remove(size - j);
                    data_acc.remove(size - j);
                    data_gyro.remove(size - j);
                    data_grav.remove(size - j);
                    serie_acc.remove(size - j);
                    serie_gyro.remove(size - j);
                    serie_grav.remove(size - j);
                    fft_freq_acc.remove(size - j);
                    fft_freq_gyro.remove(size - j);
                    fft_freq_grav.remove(size - j);
                    current_activity.remove(size - j);

                }
            }
            //transpose
            fftExcelData = utils.transpose(fftExcelData);

            fileManager.createFile(context.getFilesDir() + "/" + (filter_noise== true ? FILTERED_NOISE_FFTFILENAME : FFTFILENAME), FFTFILEHEADER);
            FileOutputStream outputStream;
            String s="";
            try {
                outputStream = context.openFileOutput((filter_noise== true ? FILTERED_NOISE_FFTFILENAME : FFTFILENAME), Context.MODE_APPEND);
                Iterator<List<String>> iter = fftExcelData.iterator();
                while (iter.hasNext()) {
                    Iterator<String> siter = iter.next().iterator();
                    s="";
                    while (siter.hasNext()) {
                        s+= siter.next() + ",";
                    }
                    //remove last comma
                    s = s.substring(0, s.length() - 1);
                    outputStream.write((s+"\n").getBytes());
                }
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }




}