package az.shlf.orderservice.service.grpc;

import az.shlf.walletservice.grpc.BalanceResponse;
import az.shlf.walletservice.grpc.CommitBalanceRequest;
import az.shlf.walletservice.grpc.ReleaseBalanceRequest;
import az.shlf.walletservice.grpc.ReserveBalanceRequest;
import az.shlf.walletservice.grpc.WalletGrpcServiceGrpc;
import az.shlf.orderservice.exception.constants.ErrorCodes;
import az.shlf.orderservice.exception.custom.CustomException;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class WalletGrpcClientService {

   @GrpcClient("wallet-service")
   private WalletGrpcServiceGrpc.WalletGrpcServiceBlockingStub walletStub;

   public void reserveBalance(String username, String asset, BigDecimal amount, String orderId) {
      ReserveBalanceRequest request = ReserveBalanceRequest.newBuilder()
              .setUsername(username)
              .setAsset(asset)
              .setAmount(amount.toString())
              .setOrderId(orderId)
              .build();

      BalanceResponse response = walletStub.reserveBalance(request);

      if (!response.getSuccess()) {
         throw new CustomException(ErrorCodes.BALANCE_RESERVE_FAILED);
      }
   }

   public void releaseBalance(String username, String asset, BigDecimal amount, String orderId) {
      ReleaseBalanceRequest request = ReleaseBalanceRequest.newBuilder()
              .setUsername(username)
              .setAsset(asset)
              .setAmount(amount.toString())
              .setOrderId(orderId)
              .build();

      BalanceResponse response = walletStub.releaseBalance(request);

      if (!response.getSuccess()) {
         throw new CustomException(ErrorCodes.BALANCE_RELEASE_FAILED);
      }
   }

   public void commitBalance(String username, String soldAsset, BigDecimal soldAmount,
                             String boughtAsset, BigDecimal boughtAmount, String orderId) {
      CommitBalanceRequest request = CommitBalanceRequest.newBuilder()
              .setUsername(username)
              .setSoldAsset(soldAsset)
              .setSoldAmount(soldAmount.toString())
              .setBoughtAsset(boughtAsset)
              .setBoughtAmount(boughtAmount.toString())
              .setOrderId(orderId)
              .build();

      BalanceResponse response = walletStub.commitBalance(request);

      if (!response.getSuccess()) {
         throw new CustomException(ErrorCodes.BALANCE_COMMIT_FAILED);
      }
   }
}