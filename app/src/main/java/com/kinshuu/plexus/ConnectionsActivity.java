package com.kinshuu.plexus;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;


public abstract class ConnectionsActivity extends AppCompatActivity {





  private static final String TAG = "Walkie_Talkie";


  private ConnectionsClient mConnectionsClient;

  /** The devices we've discovered near us. */
  private final Map<String, Endpoint> mDiscoveredEndpoints = new HashMap<>();


  private final Map<String, Endpoint> mPendingConnections = new HashMap<>();


  private final Map<String, Endpoint> mEstablishedConnections = new HashMap<>();


  private boolean mIsConnecting = false;


  private boolean mIsDiscovering = false;


  private boolean mIsAdvertising = false;


  private final ConnectionLifecycleCallback mConnectionLifecycleCallback =
      new ConnectionLifecycleCallback() {
        @Override
        public void onConnectionInitiated(String endpointId, ConnectionInfo connectionInfo) {
          logD(
              String.format(
                  "onConnectionInitiated(endpointId=%s, endpointName=%s)",
                  endpointId, connectionInfo.getEndpointName()));
          Endpoint endpoint = new Endpoint(endpointId, connectionInfo.getEndpointName());
          mPendingConnections.put(endpointId, endpoint);
          ConnectionsActivity.this.onConnectionInitiated(endpoint, connectionInfo);
        }

        @Override
        public void onConnectionResult(String endpointId, ConnectionResolution result) {
          logD(String.format("onConnectionResponse(endpointId=%s, result=%s)", endpointId, result));

          // We're no longer connecting
          mIsConnecting = false;

          if (!result.getStatus().isSuccess()) {
            logW(
                String.format(
                    "Connection failed. Received status %s.",
                    ConnectionsActivity.toString(result.getStatus())));
            onConnectionFailed(mPendingConnections.remove(endpointId));
            return;
          }
          connectedToEndpoint(mPendingConnections.remove(endpointId));
        }

        @Override
        public void onDisconnected(String endpointId) {
          if (!mEstablishedConnections.containsKey(endpointId)) {
            logW("Unexpected disconnection from endpoint " + endpointId);
            return;
          }
          disconnectedFromEndpoint(mEstablishedConnections.get(endpointId));
        }
      };


  private final PayloadCallback mPayloadCallback =
      new PayloadCallback() {
        @Override
        public void onPayloadReceived(String endpointId, Payload payload) {
          logD(String.format("onPayloadReceived(endpointId=%s, payload=%s)", endpointId, payload));
          onReceive(mEstablishedConnections.get(endpointId), payload);
        }

        @Override
        public void onPayloadTransferUpdate(String endpointId, PayloadTransferUpdate update) {
          logD(
              String.format(
                  "onPayloadTransferUpdate(endpointId=%s, update=%s)", endpointId, update));
        }
      };


  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mConnectionsClient = Nearby.getConnectionsClient(this);
  }


  protected void startAdvertising() {
    mIsAdvertising = true;
    final String localEndpointName = getName();

    AdvertisingOptions.Builder advertisingOptions = new AdvertisingOptions.Builder();
    advertisingOptions.setStrategy(getStrategy());

    mConnectionsClient
        .startAdvertising(
            localEndpointName,
            getServiceId(),
            mConnectionLifecycleCallback,
            advertisingOptions.build())
        .addOnSuccessListener(
            new OnSuccessListener<Void>() {
              @Override
              public void onSuccess(Void unusedResult) {
                logV("Now advertising endpoint " + localEndpointName);
                onAdvertisingStarted();
              }
            })
        .addOnFailureListener(
            new OnFailureListener() {
              @Override
              public void onFailure(@NonNull Exception e) {
                mIsAdvertising = false;
                logW("startAdvertising() failed.", e);
                onAdvertisingFailed();
              }
            });
  }


  protected void stopAdvertising() {
    mIsAdvertising = false;
    mConnectionsClient.stopAdvertising();
  }


  protected boolean isAdvertising() {
    return mIsAdvertising;
  }


  protected void onAdvertisingStarted() {}


  protected void onAdvertisingFailed() {}


  protected void onConnectionInitiated(Endpoint endpoint, ConnectionInfo connectionInfo) {}


  protected void acceptConnection(final Endpoint endpoint) {
    mConnectionsClient
        .acceptConnection(endpoint.getId(), mPayloadCallback)
        .addOnFailureListener(
            new OnFailureListener() {
              @Override
              public void onFailure(@NonNull Exception e) {
                logW("acceptConnection() failed.", e);
              }
            });
  }


  protected void rejectConnection(Endpoint endpoint) {
    mConnectionsClient
        .rejectConnection(endpoint.getId())
        .addOnFailureListener(
            new OnFailureListener() {
              @Override
              public void onFailure(@NonNull Exception e) {
                logW("rejectConnection() failed.", e);
              }
            });
  }


  protected void startDiscovering() {
    mIsDiscovering = true;
    mDiscoveredEndpoints.clear();
    DiscoveryOptions.Builder discoveryOptions = new DiscoveryOptions.Builder();
    discoveryOptions.setStrategy(getStrategy());
    mConnectionsClient
        .startDiscovery(
            getServiceId(),
            new EndpointDiscoveryCallback() {
              @Override
              public void onEndpointFound(String endpointId, DiscoveredEndpointInfo info) {
                logD(
                    String.format(
                        "onEndpointFound(endpointId=%s, serviceId=%s, endpointName=%s)",
                        endpointId, info.getServiceId(), info.getEndpointName()));

                if (getServiceId().equals(info.getServiceId())) {
                  Endpoint endpoint = new Endpoint(endpointId, info.getEndpointName());
                  mDiscoveredEndpoints.put(endpointId, endpoint);
                  onEndpointDiscovered(endpoint);
                }
              }

              @Override
              public void onEndpointLost(String endpointId) {
                logD(String.format("onEndpointLost(endpointId=%s)", endpointId));
              }
            },
            discoveryOptions.build())
        .addOnSuccessListener(
            new OnSuccessListener<Void>() {
              @Override
              public void onSuccess(Void unusedResult) {
                onDiscoveryStarted();
              }
            })
        .addOnFailureListener(
            new OnFailureListener() {
              @Override
              public void onFailure(@NonNull Exception e) {
                mIsDiscovering = false;
                logW("startDiscovering() failed.", e);
                onDiscoveryFailed();
              }
            });
  }


  protected void stopDiscovering() {
    mIsDiscovering = false;
    mConnectionsClient.stopDiscovery();
  }


  protected boolean isDiscovering() {
    return mIsDiscovering;
  }

   protected void onDiscoveryStarted() {}

  protected void onDiscoveryFailed() {}


  protected void onEndpointDiscovered(Endpoint endpoint) {}


  protected void disconnect(Endpoint endpoint) {
    mConnectionsClient.disconnectFromEndpoint(endpoint.getId());
    mEstablishedConnections.remove(endpoint.getId());
  }


  protected void disconnectFromAllEndpoints() {
    for (Endpoint endpoint : mEstablishedConnections.values()) {
      mConnectionsClient.disconnectFromEndpoint(endpoint.getId());
    }
    mEstablishedConnections.clear();
  }

  /** Resets and clears all state in Nearby Connections. */
  protected void stopAllEndpoints() {
    mConnectionsClient.stopAllEndpoints();
    mIsAdvertising = false;
    mIsDiscovering = false;
    mIsConnecting = false;
    mDiscoveredEndpoints.clear();
    mPendingConnections.clear();
    mEstablishedConnections.clear();
  }


  protected void connectToEndpoint(final Endpoint endpoint) {
    logV("Sending a connection request to endpoint " + endpoint);
    // Mark ourselves as connecting so we don't connect multiple times
    mIsConnecting = true;

    // Ask to connect
    mConnectionsClient
        .requestConnection(getName(), endpoint.getId(), mConnectionLifecycleCallback)
        .addOnFailureListener(
            new OnFailureListener() {
              @Override
              public void onFailure(@NonNull Exception e) {
                logW("requestConnection() failed.", e);
                mIsConnecting = false;
                onConnectionFailed(endpoint);
              }
            });
  }


  protected final boolean isConnecting() {
    return mIsConnecting;
  }

  private void connectedToEndpoint(Endpoint endpoint) {
    logD(String.format("connectedToEndpoint(endpoint=%s)", endpoint));
    mEstablishedConnections.put(endpoint.getId(), endpoint);
    onEndpointConnected(endpoint);
  }

  private void disconnectedFromEndpoint(Endpoint endpoint) {
    logD(String.format("disconnectedFromEndpoint(endpoint=%s)", endpoint));
    mEstablishedConnections.remove(endpoint.getId());
    onEndpointDisconnected(endpoint);
  }


  protected void onConnectionFailed(Endpoint endpoint) {}


  protected void onEndpointConnected(Endpoint endpoint) {}


  protected void onEndpointDisconnected(Endpoint endpoint) {}


  protected Set<Endpoint> getDiscoveredEndpoints() {
    return new HashSet<>(mDiscoveredEndpoints.values());
  }


  protected Set<Endpoint> getConnectedEndpoints() {
    return new HashSet<>(mEstablishedConnections.values());
  }


  protected void send(Payload payload) {
    send(payload, mEstablishedConnections.keySet());
  }

  private void send(Payload payload, Set<String> endpoints) {
    mConnectionsClient
        .sendPayload(new ArrayList<>(endpoints), payload)
        .addOnFailureListener(
            new OnFailureListener() {
              @Override
              public void onFailure(@NonNull Exception e) {
                logW("sendPayload() failed.", e);
              }
            });
  }


  protected void onReceive(Endpoint endpoint, Payload payload) {}





  protected abstract String getName();


  protected abstract String getServiceId();


  protected abstract Strategy getStrategy();


  private static String toString(Status status) {
    return String.format(
        Locale.US,
        "[%d]%s",
        status.getStatusCode(),
        status.getStatusMessage() != null
            ? status.getStatusMessage()
            : ConnectionsStatusCodes.getStatusCodeString(status.getStatusCode()));
  }




  @CallSuper
  protected void logV(String msg) {
    Log.v(TAG, msg);
  }

  @CallSuper
  protected void logD(String msg) {
    Log.d(TAG, msg);
  }

  @CallSuper
  protected void logW(String msg) {
    Log.w(TAG, msg);
  }

  @CallSuper
  protected void logW(String msg, Throwable e) {
    Log.w(TAG, msg, e);
  }

  @CallSuper
  protected void logE(String msg, Throwable e) {
    Log.e(TAG, msg, e);
  }

  /** Represents a device we can talk to. */
  protected static class Endpoint {
    @NonNull private final String id;
    @NonNull private final String name;

    private Endpoint(@NonNull String id, @NonNull String name) {
      this.id = id;
      this.name = name;
    }

    @NonNull
    public String getId() {
      return id;
    }

    @NonNull
    public String getName() {
      return name;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof Endpoint) {
        Endpoint other = (Endpoint) obj;
        return id.equals(other.id);
      }
      return false;
    }

    @Override
    public int hashCode() {
      return id.hashCode();
    }

    @Override
    public String toString() {
      return String.format("Endpoint{id=%s, name=%s}", id, name);
    }
  }
}
