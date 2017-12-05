package sample.lotus.com.testcipherstream;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    Tester tester;

    public static final boolean useCipher=true;
    public static final int thousandCount=5;

    Button startButton;
    TextView results;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startButton = (Button) this.findViewById(R.id.button);
        results = (TextView) this.findViewById(R.id.results);

        setupStartButton(true);

    }


    private void setupStartButton(final boolean start) {
        if(start) {
            startButton.setText(R.string.start_test);
            startButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startButton.setEnabled(false);
                    runTest();
                    displayResults();
                }
            });
        } else {
            startButton.setText(R.string.reset);
            startButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    results.setText(null);
                    setupStartButton(true);
                }
            });
        }
        startButton.setEnabled(true);
    }


    private void runTest() {
        tester=new Tester(useCipher,thousandCount);
        try {
            tester.runTest(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void displayResults() {
        results.setText(tester.getResults());
        setupStartButton(false);
    }
}
