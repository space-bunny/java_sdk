package io.fancypixel.myapplication;

import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.rabbitmq.client.ConfirmListener;
import com.rabbitmq.client.Envelope;

import java.io.IOException;

import io.spacebunny.SpaceBunny;
import io.spacebunny.connection.RabbitConnection;
import io.spacebunny.device.SBDevice;

public class MainActivity extends AppCompatActivity {

    SpaceBunny.Client spaceBunny = null;
    TextView error;
    CardView error_container;
    TextView action_refresh;

    CardView device_container;
    ProgressBar loading;
    CardView button_container;
    TextView action_subscribe;
    TextView action_publish;
    CardView publish_container;

    Toolbar toolbar;

    boolean subscribe = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set Custom view
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        error = (TextView) findViewById(R.id.error);
        error_container = (CardView) findViewById(R.id.error_container);
        action_refresh = (TextView) findViewById(R.id.action_refresh);

        device_container = (CardView) findViewById(R.id.device_container);
        loading = (ProgressBar) findViewById(R.id.loading);
        button_container = (CardView) findViewById(R.id.button_container);
        action_subscribe = (TextView) findViewById(R.id.action_subscribe);
        action_publish = (TextView) findViewById(R.id.action_publish);
        publish_container = (CardView) findViewById(R.id.publish_container);

        action_refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUI();
            }
        });

        updateUI();

    }

    public void updateUI() {
        error_container.setVisibility(View.GONE);
        device_container.setVisibility(View.GONE);
        publish_container.setVisibility(View.GONE);
        button_container.setVisibility(View.GONE);
        loading.setVisibility(View.VISIBLE);

        // Create initial interface

        final Handler mHandler = new Handler();

        new Thread(new Runnable() {
            public void run() {

                try {
                    // Set DEVICE_KEY in constants
                    spaceBunny = new SpaceBunny.Client(Constants.DEVICE_KEY);
                    spaceBunny.setOnFinishConfigiurationListener(new SpaceBunny.OnFinishConfigiurationListener() {
                        @Override
                        public void onConfigured(final SBDevice device) throws SpaceBunny.ConnectionException {
                            mHandler.post(new Runnable() {
                                public void run() {
                                    showConfigure(device);
                                }
                            });
                        }
                    });

                    //spaceBunny.setVerifyCA(false);
                    spaceBunny.connect(new SpaceBunny.OnConnectedListener() {
                        @Override
                        public void onConnected() throws SpaceBunny.ConnectionException {
                            mHandler.post(new Runnable() {
                                public void run() {
                                    showButtons();
                                }
                            });
                        }
                    });
                } catch (final SpaceBunny.ConfigurationException ex) {
                    ex.printStackTrace();
                    mHandler.post(new Runnable() {
                        public void run() {
                            showError(ex.getMessage());
                        }
                    });
                } catch (final SpaceBunny.ConnectionException ex) {
                    ex.printStackTrace();
                    mHandler.post(new Runnable() {
                        public void run() {
                            showError(ex.getMessage());
                        }
                    });
                }


            }
        }).start();
    }

    public void showError(String error_text) {
        // Interface with error
        error.setText(error_text);
        setTitle(getString(R.string.title_error));
        loading.setVisibility(View.GONE);
        button_container.setVisibility(View.GONE);
        error_container.setVisibility(View.VISIBLE);
        device_container.setVisibility(View.GONE);
        publish_container.setVisibility(View.GONE);
    }

    public void showConfigure(SBDevice device) {
        // Configuration Card with device information
        setTitle(device.getDevice_name());

        ((TextView) findViewById(R.id.text_device_name)).setText(device.getDevice_name());
        ((TextView) findViewById(R.id.text_device_id)).setText(device.getDevice_id());
        ((TextView) findViewById(R.id.text_device_host)).setText(device.getHost());
        ((TextView) findViewById(R.id.text_device_secret)).setText(device.getSecret());
        ((TextView) findViewById(R.id.text_device_vhost)).setText(device.getVhost());

        loading.setVisibility(View.GONE);
        device_container.setVisibility(View.VISIBLE);
        error_container.setVisibility(View.GONE);
        publish_container.setVisibility(View.GONE);
    }

    public void showButtons() {
        // Show buttons for publish and subscribe actions
        action_subscribe.setText(subscribe ? getString(R.string.action_unsubscribe) : getString(R.string.action_subscribe));

        final Handler mHandler = new Handler();

        action_subscribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!subscribe) {
                    try {
                        spaceBunny.subscribe(new RabbitConnection.OnSubscriptionMessageReceivedListener() {
                            @Override
                            public void onReceived(final String message, Envelope envelope) {
                                mHandler.post(new Runnable() {
                                    public void run() {
                                        Snackbar.make(toolbar, getString(R.string.text_message) + ": " +  message, Snackbar.LENGTH_LONG).show();
                                    }
                                });
                            }
                        });
                        subscribe = true;
                        showButtons();
                    } catch (SpaceBunny.ConnectionException ex) {
                        ex.printStackTrace();
                        showError(ex.getMessage());
                    }
                } else {
                    try {
                        spaceBunny.unsubscribe();
                        subscribe = false;
                        showButtons();
                    } catch (SpaceBunny.ConnectionException ex) {
                        showError(ex.getMessage());
                    }
                }
            }
        });

        action_publish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.action_close).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        publish_container.setVisibility(View.GONE);
                    }
                });

                findViewById(R.id.action_send).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            spaceBunny.publish(((EditText) findViewById(R.id.channel_to_send)).getText().toString(),
                                    ((EditText) findViewById(R.id.msg_to_send)).getText().toString(),
                                    null, new ConfirmListener() {
                                        @Override
                                        public void handleAck(long deliveryTag, boolean multiple) throws IOException {
                                            mHandler.post(new Runnable() {
                                                public void run() {
                                                    sendOk();
                                                }
                                            });
                                        }

                                        @Override
                                        public void handleNack(long deliveryTag, boolean multiple) throws IOException {
                                            mHandler.post(new Runnable() {
                                                public void run() {
                                                    sendNotOk();
                                                }
                                            });
                                        }
                                    });
                        } catch (SpaceBunny.ConnectionException ex) {
                            showError(ex.getMessage());
                        }
                    }
                });
                publish_container.setVisibility(View.VISIBLE);
            }
        });

        button_container.setVisibility(View.VISIBLE);
    }

    public void sendOk() {
        // Handle Ack
        ((EditText) findViewById(R.id.msg_to_send)).getText().clear();
        ((EditText) findViewById(R.id.channel_to_send)).getText().clear();
        publish_container.setVisibility(View.GONE);
        Snackbar.make(toolbar, getString(R.string.send_ok), Snackbar.LENGTH_SHORT).show();
    }

    public void sendNotOk() {
        // Handle Nack
        Snackbar.make(toolbar, getString(R.string.send_not_ok), Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onPause() {
        // Close connection when activity is closed
        super.onPause();
        try {
            spaceBunny.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
