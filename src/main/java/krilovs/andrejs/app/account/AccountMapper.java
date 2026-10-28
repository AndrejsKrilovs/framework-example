package krilovs.andrejs.app.account;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

/**
 * The generated implementation is annotated with jakarta.inject.Singleton, which is what
 * AppBinder scans for, so it can be injected by this interface.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA)
public interface AccountMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    Account toEntity(AccountRequest request);
}
