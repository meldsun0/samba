package samba;

import static com.google.common.base.Preconditions.checkArgument;

import samba.api.Discv5API;
import samba.api.HistoryAPI;

import java.util.Optional;

public final class SambaSDK {

  private Discv5API discv5API;
  private HistoryAPI historyAPI;

  private SambaSDK(Builder builder) {
    this.historyAPI = builder.historyAPI;
    this.discv5API = builder.discv5API;
  }

  public Optional<HistoryAPI> historyAPI() {
    return Optional.ofNullable(historyAPI);
  }

  public Optional<Discv5API> discv5API() {
    return Optional.ofNullable(discv5API);
  }

  public static final class Builder {
    private HistoryAPI historyAPI;
    private Discv5API discv5API;

    public Builder withHistoryAPI(HistoryAPI historyAPI) {
      checkArgument(historyAPI != null, "HistoryAPI must not be null");
      this.historyAPI = historyAPI;
      return this;
    }

    public Builder withDiscv5API(Discv5API discv5API) {
      checkArgument(discv5API != null, "Discv5API must not be null");
      this.discv5API = discv5API;
      return this;
    }

    public SambaSDK build() {
      return new SambaSDK(this);
    }
  }

  public static Builder builder() {
    return new Builder();
  }
}
