package com.gachiganjik.gachiganjik_server.domain.album.dto;

public record OwnershipTransferResponse(
        OwnershipTransferMemberInfo newOwner,
        OwnershipTransferMemberInfo previousOwner
) {
}
