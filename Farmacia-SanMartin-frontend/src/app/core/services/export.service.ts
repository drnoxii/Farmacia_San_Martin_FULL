import { Injectable } from '@angular/core';

export interface Hoja {
  nombre: string;
  columnas: string[];
  filas: (string | number)[][];
  dinero?: number[];   // índices de columnas que son montos (2 decimales en el PDF)
}

@Injectable({ providedIn: 'root' })
export class ExportService {

  async excel(archivo: string, hojas: Hoja[]): Promise<void> {
    const XLSX = await import('xlsx');
    const libro = XLSX.utils.book_new();

    for (const h of hojas) {
      const hoja = XLSX.utils.aoa_to_sheet([h.columnas, ...h.filas]);
      hoja['!cols'] = h.columnas.map((c, i) => ({
        wch: Math.max(c.length, ...h.filas.map(f => String(f[i] ?? '').length)) + 2
      }));
      XLSX.utils.book_append_sheet(libro, hoja, h.nombre.substring(0, 31));
    }
    XLSX.writeFile(libro, `${archivo}.xlsx`);
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