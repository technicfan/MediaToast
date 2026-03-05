package technicfan.mpristoast;

import org.endlesssource.mediainterface.api.MediaSession;
import org.endlesssource.mediainterface.api.MediaTransportControls;
import org.endlesssource.mediainterface.api.NowPlaying;
import org.endlesssource.mediainterface.api.PlaybackState;

public class Track {
    private final String sessionId;
    private final MediaTransportControls controls;
    private final String name;
    private final long startTime;
    private final boolean active;
    private final boolean changed;

    protected Track(String sessionId) {
        this(MediaTracker.getSessionById(sessionId), sessionId);
    }

    private Track(MediaSession session, String busName) {
        this.sessionId = busName;
        if (session != null) {
            this.controls = session.getControls();
            this.name = session.getNowPlaying().isPresent() ? getTrackName(session.getNowPlaying().get()) : "";
            this.active = !name.isEmpty() && !controls.getPlaybackState().equals(PlaybackState.STOPPED);
        } else {
            this.controls = null;
            this.name = "";
            this.active = false;
        }
        this.startTime = System.currentTimeMillis();
        this.changed = true;
    }

    private Track(String busName, MediaTransportControls controls, String name, long startTime, boolean active, boolean changed) {
        this.sessionId = busName;
        this.name = name;
        this.controls = controls;
        this.startTime = startTime;
        this.active = active;
        this.changed = changed;
    }

    protected String sessionId() {
        return sessionId;
    }

    protected String name() {
        return name;
    }

    protected boolean active() {
        return active;
    }

    protected boolean changed() {
        return changed;
    }

    protected void playPause() {
        if (controls != null)
            controls.togglePlayPause();
    }

    protected void play() {
        if (controls != null)
           controls.play();
    }

    protected void pause() {
        if (controls != null)
            controls.pause();
    }

    protected void next() {
        if (controls != null)
            controls.next();
    }

    protected void previous() {
        if (controls != null)
            controls.previous();
    }

    protected Track refresh() {
        String name = "";
        boolean active = false;
        MediaSession session = MediaTracker.getSessionById(sessionId);
        if (session != null) {
            name = session.getNowPlaying().isPresent() ? getTrackName(session.getNowPlaying().get()) : "";
            active = !name.isEmpty() && !controls.getPlaybackState().equals(PlaybackState.STOPPED);
        } else {
            name = "";
            active = false;
        }
        return update(name, active);
    }

    private static String getTrackName(NowPlaying info) {
        return info.getArtist().isPresent() ? String.format("%s - %s", info.getArtist().get(), info.getTitle().get()) : info.getTitle().get();
    }

    protected Track update(NowPlaying info) {
        return update(getTrackName(info), active);
    }

    protected Track update(boolean active) {
        return update(name, active);
    }

    private Track update(String name, boolean active) {
        long startTime = this.startTime;
        boolean changed = !name.equals(this.name);
        if (changed) {
            startTime = System.currentTimeMillis();
        }
        return new Track(sessionId, controls, name, startTime, active, !name.equals(this.name));
    }

    protected Track update() {
        return new Track(sessionId, controls, name, startTime, active, false);
    }

    protected float currentScrollOffset(int width) {
        //               2000 + (width - maxWidth) * 96
        //             = 2000 + (width - maxWidth) * 64 + (width - maxWidth) * 32
        //             = 2000 + (width - maxWidth) * 2^6 + (width - maxWidth) * 2^5
        long roundTime = 2000 + ((width - MediaTracker.maxWidth) << 6)
                + ((width - MediaTracker.maxWidth) << 5);
        long time = (System.currentTimeMillis() - startTime) % roundTime - 1000;
        if (time <= 0) {
            return 0;
        } else if (time >= roundTime - 2000) {
            return width - MediaTracker.maxWidth;
        }
        return time * MediaTracker.pixelPerMs;
    }
}
