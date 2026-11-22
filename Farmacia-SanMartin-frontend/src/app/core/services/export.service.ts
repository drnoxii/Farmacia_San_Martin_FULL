import { Injectable } from '@angular/core';

export interface Hoja {
  nombre: string;
  columnas: string[];
  filas: (string | number)[][];
  dinero?: number[];   // índices de columnas que son montos (2 decimales en el PDF)
}

@Injectable({ providedIn: 'root' })
export class ExportService {

  
async excel(
  archivo: string,
  hojas: Hoja[],
  titulo = 'Reporte',
  subtitulo = ''
): Promise<void> {

  const { Workbook } = await import('exceljs');

  const libro = new Workbook();
  libro.creator = 'Farmacia San Martín';

  const verde = 'FF087F73';
  const verdeClaro = 'FFE6F4F1';
  const grisClaro = 'FFF4F7F8';
  const blanco = 'FFFFFFFF';

  for (const h of hojas) {

    const nombre = h.nombre
      .replace(/[\\/*?:[\]]/g, ' ')
      .substring(0, 31);

    const hoja = libro.addWorksheet(nombre);
    const columnas = h.columnas.length;

    // ENCABEZADO INSTITUCIONAL
    hoja.mergeCells(1, 1, 1, columnas);

    const marca = hoja.getCell(1, 1);
    marca.value = 'FARMACIA SAN MARTÍN';
    marca.font = {
      name: 'Calibri',
      size: 17,
      bold: true,
      color: { argb: blanco }
    };
    marca.fill = {
      type: 'pattern',
      pattern: 'solid',
      fgColor: { argb: verde }
    };
    marca.alignment = { vertical: 'middle' };
    hoja.getRow(1).height = 40;

    // TÍTULO
    hoja.mergeCells(2, 1, 2, columnas);

    const celdaTitulo = hoja.getCell(2, 1);
    celdaTitulo.value = `${titulo} - ${h.nombre}`;
    celdaTitulo.font = {
      name: 'Calibri',
      size: 14,
      bold: true,
      color: { argb: verde }
    };
    celdaTitulo.fill = {
      type: 'pattern',
      pattern: 'solid',
      fgColor: { argb: verdeClaro }
    };
    celdaTitulo.alignment = { vertical: 'middle' };
    hoja.getRow(2).height = 32;

    // PERÍODO
    hoja.mergeCells(3, 1, 3, columnas);

    const periodo = hoja.getCell(3, 1);
    periodo.value = subtitulo;
    periodo.font = {
      name: 'Calibri',
      size: 11,
      italic: true,
      color: { argb: 'FF64748B' }
    };
    hoja.getRow(3).height = 24;

    // CABECERAS DE TABLA
    const encabezado = hoja.getRow(5);
    encabezado.values = h.columnas;
    encabezado.height = 30;

    encabezado.eachCell(celda => {
      celda.font = {
        bold: true,
        color: { argb: blanco },
        size: 11
      };
      celda.fill = {
        type: 'pattern',
        pattern: 'solid',
        fgColor: { argb: verde }
      };
      celda.alignment = {
        vertical: 'middle',
        wrapText: true
      };
    });

    // DATOS
    h.filas.forEach((fila, indice) => {

      const renglon = hoja.getRow(indice + 6);
      renglon.values = fila;
      renglon.height = 25;

      renglon.eachCell({ includeEmpty: true }, (celda, col) => {

        celda.font = {
          name: 'Calibri',
          size: 11,
          color: { argb: 'FF253444' }
        };

        celda.alignment = {
          vertical: 'middle'
        };

        if (indice % 2 === 1) {
          celda.fill = {
            type: 'pattern',
            pattern: 'solid',
            fgColor: { argb: grisClaro }
          };
        }

        // Formato monetario real de Excel
        if (h.dinero?.includes(col - 1) &&
            typeof celda.value === 'number') {

          celda.numFmt =
            '"S/ " #,##0.00;[Red]-"S/ " #,##0.00';

          celda.alignment = {
            horizontal: 'right',
            vertical: 'middle'
          };

          if (celda.value < 0) {
            celda.font = {
              bold: true,
              color: { argb: 'FFDC2626' }
            };
          }
        }
      });
    });

    // ANCHURA AUTOMÁTICA
    h.columnas.forEach((columna, i) => {

      const mayor = Math.max(
        columna.length,
        ...h.filas.map(f =>
          String(f[i] ?? '').length
        )
      );

      hoja.getColumn(i + 1).width =
        Math.min(42, Math.max(18, mayor + 4));
    });

    // Mantener visibles las cabeceras
    hoja.views = [
      { state: 'frozen', ySplit: 5 }
    ];

    // Filtros en tablas de detalle
    if (columnas > 2) {
      hoja.autoFilter = {
        from: { row: 5, column: 1 },
        to: { row: 5, column: columnas }
      };
    }

    hoja.pageSetup.orientation = 'landscape';
    hoja.pageSetup.fitToPage = true;
    hoja.pageSetup.fitToWidth = 1;
    hoja.pageSetup.fitToHeight = 0;
  }

  // DESCARGAR
  const buffer = await libro.xlsx.writeBuffer();

  const blob = new Blob(
    [new Uint8Array(buffer).buffer as ArrayBuffer],
    {
      type:
        'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    }
  );

  const url = URL.createObjectURL(blob);
  const enlace = document.createElement('a');

  enlace.href = url;
  enlace.download = `${archivo}.xlsx`;

  document.body.appendChild(enlace);
  enlace.click();
  enlace.remove();

  window.setTimeout(() => {
    URL.revokeObjectURL(url);
  }, 1000);
}


  async pdf(archivo: string, titulo: string, subtitulo: string, hojas: Hoja[]): Promise<void> {
    const { jsPDF } = await import('jspdf');
    const autoTable = (await import('jspdf-autotable')).default;

    const doc = new jsPDF({ orientation: 'landscape' });
    doc.setFontSize(16);
    doc.text('Farmacia San Martín', 14, 15);
    doc.setFontSize(12);
    doc.text(titulo, 14, 22);
    doc.setFontSize(9);
    doc.text(subtitulo, 14, 28);

    let y = 36;
    for (const h of hojas) {
      if (y > 180) { doc.addPage(); y = 20; }
      doc.setFontSize(11);
      doc.text(h.nombre, 14, y);

      autoTable(doc, {
        startY: y + 3,
        head: [h.columnas],
        body: h.filas.map(fila => fila.map((v, i) =>
          typeof v === 'number' && h.dinero?.includes(i) ? v.toFixed(2) : String(v))),
        styles: { fontSize: 8 },
        headStyles: { fillColor: [0, 137, 123] }
      });
      y = (doc as any).lastAutoTable.finalY + 10;
    }
    doc.save(`${archivo}.pdf`);
  }
}