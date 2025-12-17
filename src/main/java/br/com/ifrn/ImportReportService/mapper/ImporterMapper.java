package br.com.ifrn.ImportReportService.mapper;


import br.com.ifrn.ImportReportService.dto.ImporterDTO;
import br.com.ifrn.ImportReportService.messaging.dto.ImportMessageDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ImporterMapper {
    ImportMessageDTO toCreateClassMessageDTO(ImporterDTO dto);

    ImporterDTO toImporterDTO(ImportMessageDTO dto);
}
