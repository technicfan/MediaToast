package technicfan.mpristoast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.exceptions.DBusExecutionException;
import org.freedesktop.dbus.interfaces.Properties;
import org.freedesktop.dbus.types.Variant;

import net.minecraft.client.Minecraft;

public class Track {
    private final String busName;
    private final Player player;
    private final Scroller scroller;
    private final String name;
    private final boolean active;
    private final boolean changed;
    private final boolean existing;

    protected Track(String busName, Minecraft client, boolean existing) {
        this.busName = busName;
        this.existing = existing;
        this.player = getPlayer();
        Track temp = update(getAllValues(), null, client, true);
        this.name = temp.name;
        this.scroller = temp.scroller;
        this.active = temp.active;
        this.changed = temp.changed;
    }

    protected Track(String busName, Player player) {
        this.busName = busName;
        this.player = player;
        this.name = "";
        this.scroller = new Scroller();
        this.active = false;
        this.changed = true;
        this.existing = true;
    }

    private Track(String busName, Player player, String name, Scroller scroller, boolean active, boolean changed, boolean existing) {
        this.busName = busName;
        this.player = player;
        this.name = name;
        this.scroller = scroller;
        this.active = active;
        this.changed = changed;
        this.existing = existing;
    }

    protected String busName() {
        return busName;
    }

    protected String name() {
        return name;
    }

    protected Scroller scroller() {
        return scroller;
    }

    protected boolean active() {
        return active;
    }

    protected boolean changed() {
        return changed;
    }

    protected void playPause() {
        if (player != null)
            player.PlayPause();
    }

    protected void play() {
        if (player != null)
            player.Play();
    }

    protected void pause() {
        if (player != null)
            player.Pause();
    }

    protected void next() {
        if (player != null)
            player.Next();
    }

    protected void previous() {
        if (player != null)
            player.Previous();
    }

    protected Track refresh(Minecraft client) {
        return update(getAllValues(), null, client, true, true);
    }

    private Track update(String name, Minecraft client, boolean active, boolean existing, boolean refresh) {
        Scroller scroller = this.scroller;
        boolean changed = !name.equals(this.name);
        if (changed || refresh) {
            scroller = new Scroller(name, client);
        }
        return new Track(busName, player, name, scroller, active, !name.equals(this.name), existing);
    }

    protected Track update() {
        return new Track(busName, player, name, scroller, active, false, existing);
    }

    protected Track update(Map<String, Variant<?>> data, List<String> removed, Minecraft client, boolean init) {
        return update(data, removed, client, init, false);
    }

    private Track update(Map<String, Variant<?>> data, List<String> removed, Minecraft client, boolean init, boolean refresh) {
        String name = this.name != null ? this.name : "";
        boolean active = this.active;
        boolean existing = this.existing;
        if (!existing || init) {
            data = getAllValues();
            removed = null;
            if (!existing && !init)
                existing = true;
        }
        if (removed != null && removed.contains("Metadata")) {
            return null;
        }
        if (data.containsKey("PlaybackStatus")) {
            active = !data.get("PlaybackStatus").getValue().toString().equals("Stopped");
        }
        if (data.containsKey("Metadata")) {
            Map<?, ?> newMetadata = (Map<?, ?>) data
                    .get("Metadata")
                    .getValue();
            Map<String, Object> metadata = new HashMap<>();
            for (Map.Entry<?, ?> entry : newMetadata.entrySet()) {
                metadata.put((String) entry.getKey(), ((Variant<?>) entry.getValue()).getValue());
            }
            name = getTrackName(metadata);
        }
        return update(name, client, active, existing, refresh);
    }

    private Player getPlayer() {
        synchronized (MediaTracker.conn) {
            try {
                return MediaTracker.conn.getRemoteObject(busName, "/org/mpris/MediaPlayer2", Player.class);
            } catch (DBusException e) {
                MprisToastClient.LOGGER.warn(e.toString(), e.fillInStackTrace());
            }
            return null;
        }
    }

    private Map<String, Variant<?>> getAllValues() {
        try {
            synchronized (MediaTracker.conn) {
                Properties properties = MediaTracker.conn
                        .getRemoteObject(busName, "/org/mpris/MediaPlayer2", Properties.class);
                Map<String, Variant<?>> data = properties.GetAll("org.mpris.MediaPlayer2.Player");
                data.putAll(properties.GetAll("org.mpris.MediaPlayer2"));
                return data;
            }
        } catch (DBusException | DBusExecutionException e) {
            MprisToastClient.LOGGER.warn(e.toString(), e.fillInStackTrace());
        }
        return new HashMap<>();
    }

    private String getTrackName(Map<String, ?> metadata) {
        String track = "", artist = "";
        Object trackObj, artistsObj;
        artistsObj = metadata.get("xesam:artist");
        trackObj = metadata.get("xesam:title");
        if (artistsObj != null && artistsObj instanceof List) {
            List<?> tempList = (List<?>) artistsObj;
            List<String> list = new ArrayList<>();
            for (Object name : tempList) {
                list.add((String) name);
            }
            artist = list.isEmpty() ? "" : list.get(0);
        }
        if (trackObj != null && trackObj instanceof String) {
            track = (String) trackObj;
        }
        return artist.isEmpty() ? track : String.format("%s - %s", artist, track);
    }
}
