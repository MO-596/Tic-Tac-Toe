package com.example.tic_tac_toeapplication;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import android.media.MediaPlayer;


import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

/**
 * <p>Game board Fragment for the Tic-Tac-Toe Duel app.</p>
 *
 * <ul>
 *   <li>Builds a 3 × 3 {@link GridLayout} at runtime.</li>
 *   <li>Handles user taps, updates the underlying {@link TicTacToe} engine,
 *       and animates the UI.</li>
 *   <li>Shows AdMob interstitial every <code>amount_of_times_for_ad_to_play</code> games.</li>
 *   <li>Plays looping 8-bit background music, respecting the Fragment life-cycle.</li>
 * </ul>
 *
 * The Fragment does **not** hold game rules—those live in {@link TicTacToe}.  Keeping
 * logic and UI separate makes debugging and unit-testing simpler.
 */
public class gameFragment extends Fragment {
    TextView turnTV; // “player (X) turn” label
    TextView tvGameResults; // “player wins!” banner
    Button Play_Again_Button;
    Button Quit_Button;
    String player1Name,player2Name;

    int buttonSize; // computed square size at runtime
    int gamesPlayedCount = 0; // counts finished rounds
    int amount_of_times_for_ad_to_play = 4;    /** How many completed games trigger an interstitial ad. */

    private static final int LETTER_SIZE = 25;     /** Font size (sp) for the X / O text. */
    private Button[][] buttons; // 3 × 3 board squares
    private NavController navController;
    private InterstitialAd mInterstitialAd;
    private MediaPlayer mediaPlayer;


    TicTacToe tttGame = new TicTacToe();// constructor

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        navController = Navigation.findNavController(view); //initialize the nav controller

        // Initialise Mobile Ads SDK (async).  Load ad once ready.
        MobileAds.initialize(requireActivity(), initializationStatus -> loadInterstitialAd());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        /* ---------- inflate ---------- */
        View view = inflater.inflate(R.layout.fragment_game, container, false);

        /* ---------- start background music ---------- */
        mediaPlayer = MediaPlayer.create(requireContext(), R.raw.eight_bit_chip_tune_game_music);
        mediaPlayer.setLooping(true);  // loops the music
        mediaPlayer.start();

        /* ---------- find views ---------- */
        Play_Again_Button = view.findViewById(R.id.PlayAgain_Button);
        Quit_Button = view.findViewById(R.id.Quit_Button);

        turnTV = view.findViewById(R.id.tvTurn);
        tvGameResults = view.findViewById(R.id.tvGameResults);

        tvGameResults.setVisibility(View.INVISIBLE);
        turnTV.setVisibility(View.VISIBLE);

        /* ---------- build dynamic 3 × 3 grid ---------- */
        buildBoard(view);

        /* -------- 4. buttons -------- */
        //Buttons code is here
        Quit_Button.setOnClickListener(v -> navController.navigate(R.id.action_gameFragment_to_titleFragment));
        Play_Again_Button.setOnClickListener(v -> onPlayAgain());

// Retrieve player names from the bundle
        Bundle bundle = getArguments();
        if (bundle != null) {
            player1Name = bundle.getString("Players1Name", "Player 1");
            player2Name = bundle.getString("Players2Name", "Player 2");
        }else{
            // Use player names as needed (display them, etc.)
            player1Name = "Player 1";
            player2Name = "Player 2";
        }
        turnTV.setText(player1Name + " (X) turn");

        // Inflate the layout for this fragment
        return view;
    }

        /* ==============================================================
       BOARD CREATION
       ============================================================== */

    /** Builds the 3×3 board programmatically inside the provided layout. */
    private void buildBoard(View root) {
        //creates the layout for the GridLayout
        GridLayout gridLayout = root.findViewById(R.id.gameGrid);
        gridLayout.removeAllViews(); // // Clear any existing children

        // Calculate button size: (screenWidth - 2*16dp) / 3
        int totalWidth = getResources().getDisplayMetrics().widthPixels;
        int horizontalPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics()); // 16dp padding
        buttonSize = (totalWidth - (2 * horizontalPadding)) / TicTacToe.SIDE;

        gridLayout.setColumnCount(TicTacToe.SIDE);
        gridLayout.setRowCount(TicTacToe.SIDE);

        gridLayout.setAlignmentMode(GridLayout.ALIGN_BOUNDS);

        // Initialize the button array
        buttons = new Button[TicTacToe.SIDE][TicTacToe.SIDE];

        // Create each button, size it, style it, and add to grid
        for (int row = 0; row < TicTacToe.SIDE; row++) {
            for (int column = 0; column < TicTacToe.SIDE; column++) {
                Button b = new Button(requireContext());
                GridLayout.LayoutParams p = new GridLayout.LayoutParams();
                p.width = p.height = buttonSize;
                b.setLayoutParams(p);
                b.setTextSize(TypedValue.COMPLEX_UNIT_SP, LETTER_SIZE);
                b.setBackgroundResource(R.drawable.button_background);

                b.setTag(new int[]{row, column});            // store (row,col) for quick lookup
                b.setOnClickListener(cellClick);
                gridLayout.addView(b);
                buttons[row][column] = b;
            }
        }
    }

    /* ==============================================================
       GAME PLAY
       ============================================================== */

    /* -------------------------------------------------- click logic */
    /** One shared click-handler for all nine squares. */
    private final View.OnClickListener cellClick = v -> {
        int[] rc = (int[]) v.getTag();
        Update(rc[0], rc[1]);
    };

    /**
     * Called when a user taps a free cell.
     * - Asks the engine to play
     * - Sets the button text/color
     * - Animates the mark
     * - Checks for game over
     * @param row board row (0-based)
     * @param column board column (0-based)
     */
    public void Update(int row, int column) {
        Log.w("gameFragment", "Inside update: " + row + ", " + column);

        // ignore if already filled
        if(!buttons[row][column].getText().equals("")){
            return; // Returns empty
        }

        int turn = tttGame.Play(row,column);          // 1 = X, 2 = O
        buttons[row][column].setText(turn==1? "X":"O");
        buttons[row][column].setTextColor(ContextCompat.getColor(requireContext(), turn==1? R.color.X_color : R.color.O_color));
        animateMark(buttons[row][column]);

        // If the game ended, show results; otherwise update turn label
        if (tttGame.isGameOver()) {
           endGame();
        } else {
            // update turn label
            // Next player's turn
            turnTV.setText(tttGame.isXTurn()
                    ? player1Name + " (X) turn"
                    : player2Name + " (O) turn");
        }

    }

    /** Locks board, shows result banner, highlights win, reveals buttons. */
    private void endGame() {
        enableButtons(false);

        int winner = tttGame.whoWon();
        String msg = (winner == 0) ? "Draw!"
                : (winner == 1 ? player1Name : player2Name) + " wins!";
        tvGameResults.setText(msg);
        tvGameResults.setVisibility(View.VISIBLE);

        turnTV.setVisibility(View.INVISIBLE);// Hide the turn label once game is over

        // Highlight each winning cell with a tint
        if (winner != 0) {
            for (int[] p : tttGame.getWinTriplet()){
                highlightWin(buttons[p[0]][p[1]]);
            }
        }
        Play_Again_Button.setVisibility(View.VISIBLE);
        Quit_Button.setVisibility(View.VISIBLE);
    }

    /** Handles “PLAY AGAIN” button.
     * Clears board UI, resets engine, re-enables input, and hides result banner.
     * */
    private void onPlayAgain()
    {
        resetButtons();
        tttGame.resetGame();
        enableButtons(true); // Re-enable the buttons

        tvGameResults.setVisibility(View.INVISIBLE);
        Play_Again_Button.setVisibility(View.INVISIBLE);
        Quit_Button.setVisibility(View.INVISIBLE);

        // Increment the games played count
        gamesPlayedCount++;

        // Check if the threshold is reached to show the interstitial ad
        if (gamesPlayedCount % amount_of_times_for_ad_to_play == 0) {
            if (mInterstitialAd != null) {
                showInterstitialAd();// If the ad is loaded, show it. Otherwise, reload and show it.
                loadInterstitialAd();  // Reload the interstitial ad for the next time
            } else {
                Log.d(TAG, "The interstitial ad wasn't ready yet. Reloading...");
                loadInterstitialAd();  // Reload the interstitial ad
            }
        }

    }
    /* ==============================================================
       UI HELPERS
       ============================================================== */

    /** Enables or disables all 9 board buttons. */
    public void enableButtons(boolean enable) {
        for (Button[] row : buttons) {
            for (Button b : row) {
                b.setEnabled(enable);
            }
        }
    }

    /** Resets text, color, and background on every board button. */
    public void resetButtons() {
        for (Button[] row : buttons){
            for (Button b : row) {
                b.setText("");
                b.setTextColor(ColorUtils.setAlphaComponent(Color.WHITE, 255));
                b.setBackgroundResource(R.drawable.button_background);
            }
            // Restore initial turn label
            turnTV.setText(player1Name + " (X) turn");
            turnTV.setVisibility(View.VISIBLE);
        }
    }

    /** Simple scale animation when a mark is placed. */
    private void animateMark(View cell) {
        cell.setScaleX(0f);
        cell.setScaleY(0f);
        cell.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(150)
                .start();
    }

    /** Tints the winning cells light-green. */
    private void highlightWin(Button button) {
        int winColor = ContextCompat.getColor(requireContext(), R.color.green);
        button.setBackgroundColor(ColorUtils.setAlphaComponent(winColor, 40));
    }

    /* ==============================================================
       ADMOB
       ============================================================== */
    private void showInterstitialAd() {
        if (mInterstitialAd != null) {
            mInterstitialAd.show(requireActivity());
        } else {
            Log.d(TAG, "The interstitial ad wasn't ready yet.");
        }
    }
    private void loadInterstitialAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(requireActivity(),"ca-app-pub-7318525606558822/9504264975", adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        mInterstitialAd = interstitialAd;
                        Log.i(TAG, "onAdLoaded");
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error
                        Log.d(TAG, loadAdError.toString());
                        mInterstitialAd = null;
                    }
                });
    }
    /* ==============================================================
       MEDIA PLAYER LIFE-CYCLE
       ============================================================== */
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mediaPlayer != null) {
            mediaPlayer.start();
        }
    }

}
