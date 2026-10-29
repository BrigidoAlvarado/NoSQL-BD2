/** Sustituto del motor canvas para comprobar el contrato de presentación en pruebas. */
export const captured = { data: null as any };
export class DataSet {
  items = new Map<string | number, any>();
  constructor(items: any[] = []) { this.update(items); }
  update(items: any[]) {
    for (const item of items) this.items.set(item.id, { ...this.items.get(item.id), ...item });
  }
}
export class Network {
  constructor(_container: unknown, data: unknown) { captured.data = data; }
  on() {} destroy() {} fit() {} selectNodes() {} focus() {} unselectAll() {}
}
