package az.shlf.walletservice.grpc.controller;

import az.shlf.walletservice.grpc.*;
import az.shlf.walletservice.service.WalletService;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import java.math.BigDecimal;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class WalletGrpcController extends WalletGrpcServiceGrpc.WalletGrpcServiceImplBase {

   private final WalletService walletService;

   @Override
   public void reserveBalance(ReserveBalanceRequest request, StreamObserver<BalanceResponse> responseObserver) {
      try {
         walletService.reserveBalance(
                 request.getUsername(),
                 request.getAsset(),
                 new BigDecimal(request.getAmount()),
                 request.getOrderId()
         );

         sendSuccessResponse(responseObserver);
      } catch (Exception e) {
         log.error("ReserveBalance xətası: {}", e.getMessage());
         sendErrorResponse(responseObserver, e.getMessage());
      }
   }

   @Override
   public void releaseBalance(ReleaseBalanceRequest request, StreamObserver<BalanceResponse> responseObserver) {
      try {
         walletService.releaseBalance(
                 request.getUsername(),
                 request.getAsset(),
                 new BigDecimal(request.getAmount()),
                 request.getOrderId()
         );

         sendSuccessResponse(responseObserver);
      } catch (Exception e) {
         log.error("ReleaseBalance xətası: {}", e.getMessage());
         sendErrorResponse(responseObserver, e.getMessage());
      }
   }

   @Override
   public void commitBalance(CommitBalanceRequest request, StreamObserver<BalanceResponse> responseObserver) {
      try {
         walletService.commitBalance(
                 request.getUsername(),
                 request.getSoldAsset(),
                 new BigDecimal(request.getSoldAmount()),
                 request.getBoughtAsset(),
                 new BigDecimal(request.getBoughtAmount()),
                 request.getOrderId()
         );

         sendSuccessResponse(responseObserver);
      } catch (Exception e) {
         log.error("CommitBalance xətası: {}", e.getMessage());
         sendErrorResponse(responseObserver, e.getMessage());
      }
   }

   private void sendSuccessResponse(StreamObserver<BalanceResponse> responseObserver) {
      BalanceResponse response = BalanceResponse.newBuilder()
              .setSuccess(true)
              .setMessage("Əməliyyat uğurla icra edildi.")
              .build();
      responseObserver.onNext(response);
      responseObserver.onCompleted();
   }

   private void sendErrorResponse(StreamObserver<BalanceResponse> responseObserver, String errorMessage) {
      BalanceResponse response = BalanceResponse.newBuilder()
              .setSuccess(false)
              .setMessage(errorMessage != null ? errorMessage : "Daxili server xətası.")
              .build();
      responseObserver.onNext(response);
      responseObserver.onCompleted();
   }

}