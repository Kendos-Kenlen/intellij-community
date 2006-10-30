package com.intellij.localvcs;

import java.util.ArrayList;
import java.util.List;

public class LocalVcs {
  private Snapshot mySnapshot = new Snapshot();
  private List<Change> myPendingChanges = new ArrayList<Change>();

  public boolean hasRevision(FileName path) {
    return mySnapshot.hasRevision(path);
  }

  public Revision getRevision(FileName path) {
    return mySnapshot.getRevision(path);
  }

  public List<Revision> getRevisions(FileName path) {
    List<Revision> result = new ArrayList<Revision>();

    //todo clean up this mess
    if (!mySnapshot.hasRevision(path)) return result;

    Integer id = mySnapshot.getRevision(path).getObjectId();

    for (Snapshot snapshot : getSnapshots()) {
      Revision r = snapshot.getRevision(id);
      if (r == null) break;

      result.add(r);
    }

    return result;
  }

  public void createDirectory(FileName path) {
    myPendingChanges.add(new CreateDirectoryChange(path));
  }

  public void createFile(FileName path, String content) {
    myPendingChanges.add(new CreateFileChange(path, content));
  }

  public void changeFile(FileName path, String content) {
    myPendingChanges.add(new ChangeContentChange(path, content));
  }

  public void renameFile(FileName path, FileName newName) {
    myPendingChanges.add(new RenameFileChange(path, newName));
  }

  public void deleteFile(FileName path) {
    myPendingChanges.add(new DeleteFileChange(path));
  }

  public void commit() {
    mySnapshot = mySnapshot.apply(new ChangeSet(myPendingChanges));
    clearPendingChanges();
  }

  public void revert() {
    clearPendingChanges();

    Snapshot reverted = mySnapshot.revert();
    if (reverted == null) return;
    mySnapshot = reverted;
  }

  private void clearPendingChanges() {
    myPendingChanges = new ArrayList<Change>();
  }

  public Boolean isClean() {
    return myPendingChanges.isEmpty();
  }

  public void putLabel(String label) {
    mySnapshot.setLabel(label);
  }

  public Snapshot getSnapshot(String label) {
    for (Snapshot s : getSnapshots()) {
      if (label.equals(s.getLabel())) return s;
    }
    return null;
  }

  public List<Snapshot> getSnapshots() {
    List<Snapshot> result = new ArrayList<Snapshot>();

    Snapshot s = mySnapshot;
    while (s != null) {
      result.add(s);
      s = s.revert();
    }

    // todo bad hack, maybe replace with EmptySnapshot class
    result.remove(result.size() - 1);

    return result;
  }
}
