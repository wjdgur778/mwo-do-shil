package com.example.mwo_do_shil.config.database;

import com.example.mwo_do_shil.domain.store.dto.InputForRecommendDto;
import com.example.mwo_do_shil.external.llm.gemini.dto.InputDto;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class FirestoreService {

    private final Firestore firestore;

    public List<InputForRecommendDto> getStores(List<InputDto> filteredStores, List<InputDto> noExistIds) {
        try {
            // inputdto의 가게 이름을 noExistIds에 포함하기위해 map을 만든다.
            Map<Long, String> storesMap = new HashMap<>();
            return firestore.runTransaction(tx -> {

                List<DocumentReference> refs = filteredStores.stream()
                        .map(dto -> {
                                    Long id = dto.getId();
                                    storesMap.put(id, dto.getN());
                                    return firestore.collection("stores")
                                            .document(String.valueOf(dto.getId()));
                                }

                        )
                        .toList();

                List<DocumentSnapshot> docs =
                        tx.getAll(refs.toArray(new DocumentReference[0])).get();

                List<InputForRecommendDto> result = new ArrayList<>();

                for (DocumentSnapshot snapshot : docs) {
                    Long id = Long.valueOf(snapshot.getId());
                    if (!snapshot.exists()) {
                        noExistIds.add(new InputDto(id, storesMap.get(id)));
                    }

                    String name = snapshot.getString("name");
                    String wholeInfo = snapshot.getString("whole_info");

                    result.add(new InputForRecommendDto(id, name, wholeInfo));
                }

                return result;
            }).get();
        } catch (Exception e) {
            // 로그 꼭 찍자…
            log.error("firebase 가게 정보 조회 에러 {e ={}}", e.getMessage());
        }
        return List.of();
    }
    // batch를 통해 가게의 정보를 한번에 set
    public void setStoreWithBatch(List<InputForRecommendDto> list){
        WriteBatch batch = firestore.batch();
        try {
            for (InputForRecommendDto dto : list) {
                DocumentReference ref =
                        firestore.collection("stores")
                                .document(String.valueOf(dto.getId()));

                Map<String, Object> data = Map.of(
                        "whole_info", dto.getW(),
                        "expire_at", Timestamp.ofTimeSecondsAndNanos(
                                Instant.now().plus(7, ChronoUnit.DAYS).getEpochSecond(), 0
                        )
                );

                batch.set(ref, data, SetOptions.merge());
            }

            batch.commit().get();
        }catch (Exception e){
            log.error("firebase 가게 정보 저장 에러 {e ={}}", e.getMessage());
        }

    }
}
