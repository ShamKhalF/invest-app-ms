package az.shlf.authservice.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface MsPermissionsService {

   CompletableFuture<Map<String, Map<String, List<String>>>> getGroupedPermissions();

}
